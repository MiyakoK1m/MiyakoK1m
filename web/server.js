/**
 * Заселение и аренда — Backend
 * Node.js 22+ (built-in SQLite, no npm dependencies)
 * Run: node server.js
 *
 * Every table stores its rows as a JSON blob (id INTEGER PRIMARY KEY, json TEXT), so the
 * server needs no per-field schema and the wire format matches the Android app's Kotlin data
 * classes field-for-field. This is the single source of truth both the website and the Android
 * app read from and write to, so an edit made on one is visible on the other.
 */
'use strict';

const http = require('node:http');
const fs = require('node:fs');
const path = require('node:path');
const url = require('node:url');
const { DatabaseSync } = require('node:sqlite');

const PORT = process.env.PORT || 3000;
const DB_FILE = process.env.DB_PATH || path.join(__dirname, 'rental.db');
const PUBLIC_DIR = path.join(__dirname, 'public');
const UPLOADS_DIR = process.env.UPLOADS_PATH || path.join(__dirname, 'uploads');
fs.mkdirSync(UPLOADS_DIR, { recursive: true });

// ─────────────────────────────────────────────
// DATABASE
// ─────────────────────────────────────────────
fs.mkdirSync(path.dirname(DB_FILE), { recursive: true });
const db = new DatabaseSync(DB_FILE);

db.exec('PRAGMA journal_mode=WAL;');

const JSON_TABLES = ['trip_entries', 'rental_records', 'landlords', 'residents', 'cities', 'finance_registry'];
for (const table of JSON_TABLES) {
  db.exec(`
    CREATE TABLE IF NOT EXISTS ${table} (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      json TEXT NOT NULL,
      updated_at INTEGER DEFAULT (unixepoch('now') * 1000)
    );
  `);
}

db.exec(`
  CREATE TABLE IF NOT EXISTS shared_fields (
    id INTEGER PRIMARY KEY CHECK (id = 1),
    json TEXT NOT NULL
  );
`);

db.exec(`
  CREATE TABLE IF NOT EXISTS templates (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL
  );
`);

const DEFAULT_CITIES = [
  'Астана', 'Алматы', 'Шымкент', 'Караганда', 'Актобе', 'Атырау',
  'Усть-Каменогорск', 'Тараз', 'Талдыкорган', 'Уральск', 'Костанай',
  'Кызылорда', 'Актау', 'Павлодар', 'Петропавловск', 'Туркестан',
  'Жезказган', 'Конаев', 'Семей', 'Кокшетау',
];

const DEFAULT_TEMPLATES = {
  CHECKIN: `Информация по заселению

ЖК: {жк}
Адрес: {адрес}
Подъезд: {подъезд}
Этаж: {этаж}
Квартира №: {номер_квартиры}
Код домофона: {домофон}

Заселение: {дата_заезда} с {время_заезда}
Выселение: {дата_выезда} до {время_выезда}

2ГИС: {2гис}

Wi-Fi: {wifi_имя}
Пароль: {wifi_пароль}

{правила}
{комментарий}`,
  BOOKING: `Подтверждение бронирования

Гости: {фио_гостей}
Город: {город}
ЖК: {жк}
Адрес: {адрес}
Тип квартиры: {тип_квартиры}

Заезд: {дата_заезда} {время_заезда}
Выезд: {дата_выезда} {время_выезда}

Стоимость за сутки: {ставка}
Депозит: {депозит}
Итого: {итого}`,
  FINANCE_OBJECT: `Город: {город}
ФИО: {фио}
Тип квартиры: {тип_квартиры}
Количество гостей: {гостей}
Заезд: {дата_заезда}
Выезд: {дата_выезда}
Стоимость за сутки: {ставка}
Депозит: {депозит}
Итого: {итого}
Комментарий: {комментарий}`,
};

function seed() {
  const cityCount = db.prepare('SELECT COUNT(*) as n FROM cities').get();
  if (cityCount.n === 0) {
    const ins = db.prepare('INSERT INTO cities (json) VALUES (?)');
    for (const name of DEFAULT_CITIES) ins.run(JSON.stringify({ name }));
  }
  const sharedCount = db.prepare('SELECT COUNT(*) as n FROM shared_fields').get();
  if (sharedCount.n === 0) {
    db.prepare('INSERT INTO shared_fields (id, json) VALUES (1, ?)').run(JSON.stringify(defaultSharedFields()));
  }
  for (const [key, value] of Object.entries(DEFAULT_TEMPLATES)) {
    db.prepare('INSERT OR IGNORE INTO templates (key, value) VALUES (?, ?)').run(key, value);
  }
}

function defaultSharedFields() {
  return {
    guestNames: [''],
    city: '',
    address: '',
    complexName: '',
    apartmentTypeName: 'ONE_ROOM',
    apartmentTypeOtherText: '',
    checkinDateIso: null,
    checkoutDateIso: null,
    checkinTime: '14:00',
    checkoutTime: '12:00',
    dailyRate: 0,
    deposit: 0,
  };
}

seed();

// ─────────────────────────────────────────────
// GENERIC JSON-TABLE CRUD
// ─────────────────────────────────────────────
function rowToObject(row) {
  const obj = JSON.parse(row.json);
  obj.id = row.id;
  return obj;
}

function listAll(table) {
  const rows = db.prepare(`SELECT id, json FROM ${table} ORDER BY id DESC`).all();
  return rows.map(rowToObject);
}

function insertRow(table, obj) {
  const { id, ...rest } = obj;
  const info = db.prepare(`INSERT INTO ${table} (json) VALUES (?)`).run(JSON.stringify(rest));
  return { ...rest, id: Number(info.lastInsertRowid) };
}

function updateRow(table, id, obj) {
  const { id: _ignored, ...rest } = obj;
  db.prepare(`UPDATE ${table} SET json = ?, updated_at = unixepoch('now') * 1000 WHERE id = ?`).run(JSON.stringify(rest), id);
  return { ...rest, id: Number(id) };
}

function deleteRow(table, id) {
  db.prepare(`DELETE FROM ${table} WHERE id = ?`).run(id);
}

function clearTable(table) {
  db.prepare(`DELETE FROM ${table}`).run();
}

// ─────────────────────────────────────────────
// HTTP HELPERS
// ─────────────────────────────────────────────
function sendJson(res, status, data) {
  const body = JSON.stringify(data);
  res.writeHead(status, {
    'Content-Type': 'application/json; charset=utf-8',
    'Content-Length': Buffer.byteLength(body),
    'Access-Control-Allow-Origin': '*',
  });
  res.end(body);
}

function readBody(req) {
  return new Promise((resolve, reject) => {
    let data = '';
    req.on('data', (chunk) => { data += chunk; });
    req.on('end', () => {
      if (!data) return resolve({});
      try {
        resolve(JSON.parse(data));
      } catch (e) {
        reject(e);
      }
    });
    req.on('error', reject);
  });
}

function readRawBody(req) {
  return new Promise((resolve, reject) => {
    const chunks = [];
    req.on('data', (chunk) => chunks.push(chunk));
    req.on('end', () => resolve(Buffer.concat(chunks)));
    req.on('error', reject);
  });
}

function sanitizeFileName(name) {
  return String(name || 'файл').replace(/[\\/:*?"<>|]/g, '_').slice(0, 180);
}

const MIME = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.svg': 'image/svg+xml',
};

function serveStatic(req, res, pathname) {
  let filePath = path.join(PUBLIC_DIR, pathname === '/' ? 'index.html' : pathname);
  if (!filePath.startsWith(PUBLIC_DIR)) {
    res.writeHead(403); res.end('Forbidden'); return;
  }
  fs.readFile(filePath, (err, content) => {
    if (err) {
      fs.readFile(path.join(PUBLIC_DIR, 'index.html'), (err2, indexContent) => {
        if (err2) { res.writeHead(404); res.end('Not found'); return; }
        res.writeHead(200, { 'Content-Type': MIME['.html'] });
        res.end(indexContent);
      });
      return;
    }
    const ext = path.extname(filePath);
    res.writeHead(200, { 'Content-Type': MIME[ext] || 'application/octet-stream' });
    res.end(content);
  });
}

// table name -> URL segment
const TABLE_ROUTES = {
  'trip-entries': 'trip_entries',
  'rental-records': 'rental_records',
  'landlords': 'landlords',
  'residents': 'residents',
  'cities': 'cities',
  'finance-registry': 'finance_registry',
};

const server = http.createServer(async (req, res) => {
  const parsed = url.parse(req.url, true);
  const pathname = parsed.pathname;

  if (req.method === 'OPTIONS') {
    res.writeHead(204, {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET,POST,PUT,DELETE,OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type',
    });
    res.end();
    return;
  }

  if (pathname.startsWith('/uploads/')) {
    const filePath = path.join(UPLOADS_DIR, path.basename(pathname));
    fs.readFile(filePath, (err, content) => {
      if (err) { res.writeHead(404); res.end('Not found'); return; }
      res.writeHead(200, { 'Content-Type': 'application/octet-stream', 'Access-Control-Allow-Origin': '*' });
      res.end(content);
    });
    return;
  }

  if (!pathname.startsWith('/api/')) {
    serveStatic(req, res, pathname);
    return;
  }

  try {
    const segments = pathname.split('/').filter(Boolean); // ['api', 'trip-entries', '3']
    const resource = segments[1];
    const idSegment = segments[2];

    // ---- generic JSON-table resources ----
    if (resource && TABLE_ROUTES[resource]) {
      const table = TABLE_ROUTES[resource];

      if (req.method === 'GET' && !idSegment) {
        return sendJson(res, 200, listAll(table));
      }
      if (req.method === 'POST' && !idSegment) {
        const body = await readBody(req);
        return sendJson(res, 201, insertRow(table, body));
      }
      if (req.method === 'PUT' && idSegment) {
        const body = await readBody(req);
        return sendJson(res, 200, updateRow(table, Number(idSegment), body));
      }
      if (req.method === 'DELETE' && idSegment) {
        deleteRow(table, Number(idSegment));
        return sendJson(res, 200, { ok: true });
      }
      if (req.method === 'DELETE' && !idSegment) {
        clearTable(table);
        return sendJson(res, 200, { ok: true });
      }
    }

    // ---- shared fields (single-row sync bus) ----
    if (resource === 'shared-fields') {
      if (req.method === 'GET') {
        const row = db.prepare('SELECT json FROM shared_fields WHERE id = 1').get();
        return sendJson(res, 200, row ? JSON.parse(row.json) : defaultSharedFields());
      }
      if (req.method === 'PUT') {
        const body = await readBody(req);
        const current = JSON.parse((db.prepare('SELECT json FROM shared_fields WHERE id = 1').get() || {}).json || '{}');
        const merged = { ...defaultSharedFields(), ...current, ...body };
        db.prepare('UPDATE shared_fields SET json = ? WHERE id = 1').run(JSON.stringify(merged));
        return sendJson(res, 200, merged);
      }
    }

    // ---- templates ----
    if (resource === 'templates') {
      if (req.method === 'GET' && !idSegment) {
        const rows = db.prepare('SELECT key, value FROM templates').all();
        const obj = {};
        for (const r of rows) obj[r.key] = r.value;
        return sendJson(res, 200, obj);
      }
      if (req.method === 'PUT' && idSegment) {
        const body = await readBody(req);
        db.prepare('INSERT INTO templates (key, value) VALUES (?, ?) ON CONFLICT(key) DO UPDATE SET value = excluded.value')
          .run(idSegment, body.value || '');
        return sendJson(res, 200, { ok: true });
      }
    }

    // ---- cities: reset to default ----
    if (resource === 'cities-reset' && req.method === 'POST') {
      clearTable('cities');
      const ins = db.prepare('INSERT INTO cities (json) VALUES (?)');
      for (const name of DEFAULT_CITIES) ins.run(JSON.stringify({ name }));
      return sendJson(res, 200, listAll('cities'));
    }

    // ---- nuke everything ----
    if (resource === 'all-data' && req.method === 'DELETE') {
      for (const table of JSON_TABLES) clearTable(table);
      db.prepare('DELETE FROM cities').run();
      const ins = db.prepare('INSERT INTO cities (json) VALUES (?)');
      for (const name of DEFAULT_CITIES) ins.run(JSON.stringify({ name }));
      db.prepare('UPDATE shared_fields SET json = ? WHERE id = 1').run(JSON.stringify(defaultSharedFields()));
      return sendJson(res, 200, { ok: true });
    }

    if (resource === 'health') {
      return sendJson(res, 200, { ok: true, time: new Date().toISOString() });
    }

    // ---- receipt file upload: raw bytes in the body, original name in a header ----
    if (resource === 'upload' && req.method === 'POST') {
      const original = sanitizeFileName(decodeURIComponent(req.headers['x-original-filename'] || 'file'));
      const stamped = `${Date.now()}_${original}`;
      const bytes = await readRawBody(req);
      fs.writeFileSync(path.join(UPLOADS_DIR, stamped), bytes);
      return sendJson(res, 201, { url: `/uploads/${stamped}`, fileName: original });
    }

    sendJson(res, 404, { error: 'Not found' });
  } catch (err) {
    console.error(err);
    sendJson(res, 500, { error: String(err.message || err) });
  }
});

server.listen(PORT, () => {
  console.log(`[Заселение и аренда] Server listening on http://localhost:${PORT}`);
  console.log(`[DB] ${DB_FILE}`);
});
