/* Thin fetch wrapper around the backend's REST API (see server.js). */

const Api = {
  async _json(res) {
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return res.status === 204 ? null : res.json();
  },

  list(resource) {
    return fetch(`/api/${resource}`).then(Api._json);
  },
  create(resource, data) {
    return fetch(`/api/${resource}`, {
      method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(data),
    }).then(Api._json);
  },
  update(resource, id, data) {
    return fetch(`/api/${resource}/${id}`, {
      method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(data),
    }).then(Api._json);
  },
  remove(resource, id) {
    return fetch(`/api/${resource}/${id}`, { method: 'DELETE' }).then(Api._json);
  },
  clear(resource) {
    return fetch(`/api/${resource}`, { method: 'DELETE' }).then(Api._json);
  },

  getSharedFields() {
    return fetch('/api/shared-fields').then(Api._json);
  },
  updateSharedFields(patch) {
    return fetch('/api/shared-fields', {
      method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(patch),
    }).then(Api._json);
  },

  getTemplates() {
    return fetch('/api/templates').then(Api._json);
  },
  updateTemplate(key, value) {
    return fetch(`/api/templates/${key}`, {
      method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ value }),
    }).then(Api._json);
  },

  resetCities() {
    return fetch('/api/cities-reset', { method: 'POST' }).then(Api._json);
  },
  deleteAllData() {
    return fetch('/api/all-data', { method: 'DELETE' }).then(Api._json);
  },
};
