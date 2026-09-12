/**
 * Google Drive persistence for the SQLite database file — no npm dependencies (just the
 * Node built-ins `https` and `crypto`), so it fits the project's zero-dependency approach.
 *
 * Why: a free-tier host (e.g. Render's free web service) has no persistent disk, so the SQLite
 * file would be wiped on every restart/redeploy/sleep cycle. Instead of paying for a disk, this
 * backs the file up to a Google Drive folder the user shares with a service account, and restores
 * it from there on boot.
 *
 * Setup (documented in web/README.md):
 *   1. Create a Google Cloud project (free) and a Service Account in it.
 *   2. Enable the Google Drive API for that project.
 *   3. Create a JSON key for the service account and set its whole contents as the
 *      GDRIVE_SERVICE_ACCOUNT_JSON environment variable.
 *   4. Create a folder in the user's own Google Drive, share it with the service account's
 *      email (найдётся в client_email внутри JSON-ключа) with "Editor" access, and set that
 *      folder's ID (from its URL) as GDRIVE_FOLDER_ID.
 * Leaving either env var unset disables Drive sync entirely — the server just behaves as it did
 * before (whatever's on local disk, ephemeral or not).
 */
'use strict';

const https = require('node:https');
const crypto = require('node:crypto');
const fs = require('node:fs');

function base64url(input) {
  const buf = Buffer.isBuffer(input) ? input : Buffer.from(input);
  return buf.toString('base64').replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

function httpRequest(options, body) {
  return new Promise((resolve, reject) => {
    const req = https.request(options, (res) => {
      const chunks = [];
      res.on('data', (chunk) => chunks.push(chunk));
      res.on('end', () => {
        const buffer = Buffer.concat(chunks);
        resolve({ statusCode: res.statusCode, bodyBuffer: buffer, bodyText: buffer.toString('utf8') });
      });
    });
    req.on('error', reject);
    if (body) req.write(body);
    req.end();
  });
}

class DriveSync {
  constructor({ serviceAccountJson, folderId, fileName }) {
    this.folderId = folderId;
    this.fileName = fileName || 'rental.db.backup';
    this.cachedToken = null;
    this.tokenExpiresAtSec = 0;
    this.enabled = false;

    if (!serviceAccountJson || !folderId) return;
    try {
      const creds = JSON.parse(serviceAccountJson);
      this.clientEmail = creds.client_email;
      this.privateKey = creds.private_key;
      this.enabled = Boolean(this.clientEmail && this.privateKey);
    } catch (e) {
      console.error('[DriveSync] GDRIVE_SERVICE_ACCOUNT_JSON is not valid JSON:', e.message);
    }
  }

  async getAccessToken() {
    const nowSec = Math.floor(Date.now() / 1000);
    if (this.cachedToken && nowSec < this.tokenExpiresAtSec - 60) return this.cachedToken;

    const header = { alg: 'RS256', typ: 'JWT' };
    const claims = {
      iss: this.clientEmail,
      scope: 'https://www.googleapis.com/auth/drive.file',
      aud: 'https://oauth2.googleapis.com/token',
      iat: nowSec,
      exp: nowSec + 3600,
    };
    const unsigned = `${base64url(JSON.stringify(header))}.${base64url(JSON.stringify(claims))}`;
    const signature = crypto.sign('RSA-SHA256', Buffer.from(unsigned), this.privateKey);
    const assertion = `${unsigned}.${base64url(signature)}`;

    const body = new URLSearchParams({
      grant_type: 'urn:ietf:params:oauth:grant-type:jwt-bearer',
      assertion,
    }).toString();

    const response = await httpRequest({
      hostname: 'oauth2.googleapis.com',
      path: '/token',
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Content-Length': Buffer.byteLength(body),
      },
    }, body);

    const json = JSON.parse(response.bodyText);
    if (!json.access_token) throw new Error(`Google OAuth token request failed: ${response.bodyText}`);
    this.cachedToken = json.access_token;
    this.tokenExpiresAtSec = nowSec + (json.expires_in || 3600);
    return this.cachedToken;
  }

  async findFileId() {
    const token = await this.getAccessToken();
    const escapedName = this.fileName.replace(/'/g, "\\'");
    const q = encodeURIComponent(`name='${escapedName}' and '${this.folderId}' in parents and trashed=false`);
    const response = await httpRequest({
      hostname: 'www.googleapis.com',
      path: `/drive/v3/files?q=${q}&fields=files(id,name)&spaces=drive`,
      method: 'GET',
      headers: { Authorization: `Bearer ${token}` },
    });
    if (response.statusCode !== 200) {
      throw new Error(`Drive file search failed (${response.statusCode}): ${response.bodyText}`);
    }
    const json = JSON.parse(response.bodyText);
    return json.files && json.files.length > 0 ? json.files[0].id : null;
  }

  /** Downloads the backup into `destPath`; returns false (without throwing) if there's nothing to restore yet. */
  async downloadTo(destPath) {
    if (!this.enabled) return false;
    try {
      const fileId = await this.findFileId();
      if (!fileId) return false;
      const token = await this.getAccessToken();
      const response = await httpRequest({
        hostname: 'www.googleapis.com',
        path: `/drive/v3/files/${fileId}?alt=media`,
        method: 'GET',
        headers: { Authorization: `Bearer ${token}` },
      });
      if (response.statusCode !== 200) {
        console.error(`[DriveSync] download failed (${response.statusCode}): ${response.bodyText}`);
        return false;
      }
      fs.writeFileSync(destPath, response.bodyBuffer);
      return true;
    } catch (e) {
      console.error('[DriveSync] download failed:', e.message);
      return false;
    }
  }

  /** Uploads `srcPath`'s current bytes as the backup, creating it on first run and updating it after. */
  async uploadFrom(srcPath) {
    if (!this.enabled) return false;
    try {
      const existingId = await this.findFileId();
      const token = await this.getAccessToken();
      const fileBytes = fs.readFileSync(srcPath);
      const boundary = `drivesync_${crypto.randomBytes(12).toString('hex')}`;
      const metadata = existingId ? { name: this.fileName } : { name: this.fileName, parents: [this.folderId] };
      const body = Buffer.concat([
        Buffer.from(`--${boundary}\r\nContent-Type: application/json; charset=UTF-8\r\n\r\n${JSON.stringify(metadata)}\r\n`, 'utf8'),
        Buffer.from(`--${boundary}\r\nContent-Type: application/octet-stream\r\n\r\n`, 'utf8'),
        fileBytes,
        Buffer.from(`\r\n--${boundary}--`, 'utf8'),
      ]);

      const uploadPath = existingId
        ? `/upload/drive/v3/files/${existingId}?uploadType=multipart`
        : '/upload/drive/v3/files?uploadType=multipart';

      const response = await httpRequest({
        hostname: 'www.googleapis.com',
        path: uploadPath,
        method: existingId ? 'PATCH' : 'POST',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': `multipart/related; boundary=${boundary}`,
          'Content-Length': body.length,
        },
      }, body);

      if (response.statusCode < 200 || response.statusCode >= 300) {
        console.error(`[DriveSync] upload failed (${response.statusCode}): ${response.bodyText}`);
        return false;
      }
      return true;
    } catch (e) {
      console.error('[DriveSync] upload failed:', e.message);
      return false;
    }
  }
}

module.exports = { DriveSync };
