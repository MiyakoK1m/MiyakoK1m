/* Shared helpers used across every section. */

const DEPARTMENTS = [
  'IT', 'БиОТ', 'Инфраструктура', 'Коммерческий департамент', 'Логистика',
  'НО', 'Отдел кадров', 'Проектный и процессный отдел', 'СБ', 'СП', 'Склад',
];

const AIRPORT_CODES = [
  'NQZ — Астана', 'ALA — Алматы', 'CIT — Шымкент', 'KGF — Караганда',
  'AKX — Актобе', 'GUW — Атырау', 'UKK — Усть-Каменогорск', 'DMB — Тараз',
  'TDK — Талдыкорган', 'URA — Уральск', 'KSN — Костанай', 'KZO — Кызылорда',
  'SCO — Актау', 'PWQ — Павлодар', 'PPK — Петропавловск', 'HSA — Туркестан',
  'DZN — Жезказган', 'GYD — Конаев', 'PLX — Семей', 'KOV — Кокшетау',
];

const APARTMENT_TYPES = [
  ['STUDIO', 'Студия'], ['ONE_ROOM', '1-комнатная'], ['TWO_ROOM', '2-комнатная'],
  ['THREE_ROOM', '3-комнатная'], ['FOUR_ROOM', '4-комнатная'], ['OTHER', 'Другое'],
];

function apartmentTypeLabel(code) {
  return (APARTMENT_TYPES.find((t) => t[0] === code) || [null, code])[1];
}

function apartmentRoomsCount(code) {
  return { STUDIO: 0, ONE_ROOM: 1, TWO_ROOM: 2, THREE_ROOM: 3, FOUR_ROOM: 4, OTHER: 0 }[code] ?? 0;
}

/** "today" must always be Asia/Almaty (UTC+5), regardless of the visitor's own time zone. */
function todayAlmaty() {
  const parts = new Intl.DateTimeFormat('en-CA', {
    timeZone: 'Asia/Almaty', year: 'numeric', month: '2-digit', day: '2-digit',
  }).formatToParts(new Date());
  const map = Object.fromEntries(parts.map((p) => [p.type, p.value]));
  return `${map.year}-${map.month}-${map.day}`; // ISO yyyy-MM-dd, safe to string-compare
}

function formatDate(iso) {
  if (!iso) return '';
  const [y, m, d] = iso.split('-');
  return `${d}.${m}.${y}`;
}

function parseDateFlexible(text) {
  if (!text) return null;
  const t = text.trim();
  let m = t.match(/^(\d{2})\.(\d{2})\.(\d{4})$/);
  if (m) return `${m[3]}-${m[2]}-${m[1]}`;
  m = t.match(/^(\d{4})-(\d{2})-(\d{2})$/);
  if (m) return t;
  return null;
}

function formatMoney(n) {
  const value = Math.round(Number(n) || 0);
  const sign = value < 0 ? '-' : '';
  const digits = Math.abs(value).toString();
  const grouped = digits.replace(/\B(?=(\d{3})+(?!\d))/g, ' ');
  return `${sign}${grouped} ₸`;
}

function isActiveToday(startIso, endIso, today = todayAlmaty()) {
  if (!startIso || !endIso) return false;
  return startIso <= today && today <= endIso;
}

function rangesOverlap(aStart, aEnd, bStart, bEnd) {
  if (!aStart || !aEnd || !bStart || !bEnd) return false;
  return aStart <= bEnd && bStart <= aEnd;
}

function daysBetween(startIso, endIso) {
  if (!startIso || !endIso) return 0;
  const d = (new Date(endIso) - new Date(startIso)) / 86400000;
  return d > 0 ? Math.round(d) : 0;
}

function normalizeName(name) {
  return name.trim().toLowerCase().split(/\s+/).filter(Boolean).sort().join(' ');
}

function sameName(a, b) {
  if (!a || !b) return false;
  const na = normalizeName(a);
  const nb = normalizeName(b);
  return !!na && na === nb;
}

function renderTemplate(template, tokens) {
  let result = template;
  for (const [key, value] of Object.entries(tokens)) {
    result = result.split(`{${key}}`).join(value ?? '');
  }
  return result;
}

function escapeHtml(text) {
  const div = document.createElement('div');
  div.textContent = text ?? '';
  return div.innerHTML;
}

function debounce(fn, ms) {
  let handle;
  return (...args) => {
    clearTimeout(handle);
    handle = setTimeout(() => fn(...args), ms);
  };
}

function downloadCsv(filename, headers, rows) {
  const escapeCell = (v) => {
    const s = String(v ?? '');
    return /[",\n;]/.test(s) ? `"${s.replace(/"/g, '""')}"` : s;
  };
  const lines = [headers.map(escapeCell).join(','), ...rows.map((r) => r.map(escapeCell).join(','))];
  const blob = new Blob(['﻿' + lines.join('\r\n')], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  link.download = filename;
  link.click();
  URL.revokeObjectURL(link.href);
}

function downloadXlsx(filename, sheets) {
  // sheets: [{ name, headers, rows }]
  const wb = XLSX.utils.book_new();
  for (const sheet of sheets) {
    const aoa = [sheet.headers, ...sheet.rows];
    const ws = XLSX.utils.aoa_to_sheet(aoa);
    XLSX.utils.book_append_sheet(wb, ws, sheet.name.slice(0, 31));
  }
  XLSX.writeFile(wb, filename);
}

function matchHeaderIndex(headers, target) {
  const norm = (s) => s.trim().toLowerCase();
  const nt = norm(target);
  let idx = headers.findIndex((h) => norm(h) === nt);
  if (idx >= 0) return idx;
  idx = headers.findIndex((h) => norm(h).includes(nt) || nt.includes(norm(h)));
  return idx >= 0 ? idx : null;
}

function readXlsxOrCsv(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onerror = reject;
    reader.onload = () => {
      try {
        const isCsv = file.name.toLowerCase().endsWith('.csv');
        const wb = isCsv
          ? XLSX.read(reader.result, { type: 'binary' })
          : XLSX.read(reader.result, { type: 'array' });
        const sheet = wb.Sheets[wb.SheetNames[0]];
        const rows = XLSX.utils.sheet_to_json(sheet, { header: 1, raw: false, defval: '' });
        if (rows.length === 0) return resolve({ headers: [], rows: [] });
        const [headers, ...body] = rows;
        resolve({ headers, rows: body.filter((r) => r.some((c) => String(c).trim() !== '')) });
      } catch (e) {
        reject(e);
      }
    };
    if (file.name.toLowerCase().endsWith('.csv')) reader.readAsBinaryString(file);
    else reader.readAsArrayBuffer(file);
  });
}

function el(tag, attrs = {}, children = []) {
  const node = document.createElement(tag);
  for (const [k, v] of Object.entries(attrs)) {
    if (k === 'class') node.className = v;
    else if (k.startsWith('on') && typeof v === 'function') node.addEventListener(k.slice(2), v);
    else if (v !== null && v !== undefined) node.setAttribute(k, v);
  }
  for (const child of [].concat(children)) {
    if (child == null) continue;
    node.appendChild(child instanceof Node ? child : document.createTextNode(String(child)));
  }
  return node;
}

/* ---- shared small form-building helpers, used by every section ---- */

function field(label, inputEl) {
  const wrap = el('div', { class: 'field' });
  wrap.appendChild(el('label', {}, label));
  wrap.appendChild(inputEl);
  return wrap;
}

function selectEl(options, selected, onChange) {
  const select = el('select', {});
  for (const opt of options) {
    select.appendChild(el('option', { value: opt, selected: opt === selected ? 'selected' : null }, opt));
  }
  select.addEventListener('change', () => onChange(select.value));
  return select;
}

function datalistInput(listId, options, value) {
  const wrap = el('div');
  const input = el('input', { type: 'text', value: value || '', list: listId });
  const datalist = el('datalist', { id: listId });
  options.forEach((o) => datalist.appendChild(el('option', { value: o })));
  wrap.appendChild(input);
  wrap.appendChild(datalist);
  return wrap;
}

function tilePicker(options, selected, onChange, isRed) {
  const wrap = el('div', { class: 'tiles' });
  function refresh() {
    wrap.querySelectorAll('.tile').forEach((t) => {
      const active = t.dataset.value === selected;
      t.classList.toggle('selected', active);
      t.classList.toggle('red', active && isRed && isRed(t.dataset.value));
    });
  }
  for (const [value, label] of options) {
    const tile = el('div', { class: 'tile', 'data-value': value }, label);
    tile.addEventListener('click', () => { selected = value; onChange(value); refresh(); });
    wrap.appendChild(tile);
  }
  refresh();
  return wrap;
}

function moneyInput(value, onChange) {
  const input = el('input', { type: 'number', value: value || '', placeholder: '0' });
  input.addEventListener('change', () => onChange(Number(input.value) || 0));
  return input;
}

function confirmDeleteAllDialog(onConfirm) {
  const backdrop = el('div', { class: 'modal-backdrop' });
  const modal = el('div', { class: 'modal' });
  modal.appendChild(el('h3', {}, 'Удалить ВСЕ данные приложения?'));
  modal.appendChild(el('p', {}, 'Это удалит базу аренды, входную информацию, арендодателей и все остальные разделы без возможности восстановления. Чтобы подтвердить, введите слово "УДАЛИТЬ".'));
  const input = el('input', { type: 'text' });
  modal.appendChild(input);
  const actions = el('div', { class: 'pill-actions', style: 'margin-top:14px' });
  const confirmBtn = el('button', { class: 'danger', disabled: 'disabled' }, 'Удалить всё');
  input.addEventListener('input', () => { confirmBtn.disabled = input.value.trim() !== 'УДАЛИТЬ'; });
  confirmBtn.addEventListener('click', () => { document.body.removeChild(backdrop); onConfirm(); });
  const cancelBtn = el('button', { class: 'secondary' }, 'Отмена');
  cancelBtn.addEventListener('click', () => document.body.removeChild(backdrop));
  actions.appendChild(confirmBtn);
  actions.appendChild(cancelBtn);
  modal.appendChild(actions);
  backdrop.appendChild(modal);
  document.body.appendChild(backdrop);
}
