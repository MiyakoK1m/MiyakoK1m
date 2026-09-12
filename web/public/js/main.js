/* Sidebar nav + theme toggle + router. Each section module registers itself on `Sections`. */

const Sections = {};

const NAV_ITEMS = [
  ['trip-input', 'Входная информация'],
  ['dashboard', 'Дэшборд'],
  ['calendar', 'Календарь'],
  ['checkin-info', 'Информация по заселению'],
  ['booking', 'Бронирование'],
  ['finance-approval', 'Согласование с финансами'],
  ['rental-database', 'База данных'],
  ['rental-info-table', 'Информация по аренде'],
  ['landlords', 'Арендодатели'],
  ['residents', 'Проживающие сотрудники'],
  ['cities', 'Города'],
];

function applyTheme(isDark) {
  document.documentElement.setAttribute('data-theme', isDark ? 'dark' : 'light');
  localStorage.setItem('theme-dark', isDark ? '1' : '0');
  const btn = document.getElementById('theme-toggle');
  if (btn) btn.textContent = isDark ? '☀️ Светлая тема' : '🌙 Тёмная тема';
}

function initTheme() {
  const stored = localStorage.getItem('theme-dark');
  const isDark = stored === null ? window.matchMedia('(prefers-color-scheme: dark)').matches : stored === '1';
  applyTheme(isDark);
}

function currentRoute() {
  return (location.hash || '#trip-input').slice(1);
}

async function renderRoute() {
  const route = currentRoute();
  document.querySelectorAll('.nav-item').forEach((n) => n.classList.toggle('active', n.dataset.route === route));
  const main = document.getElementById('content');
  main.innerHTML = '<p class="small-muted">Загрузка…</p>';
  const section = Sections[route] || Sections['trip-input'];
  try {
    await section.render(main);
  } catch (err) {
    console.error(err);
    main.innerHTML = `<p style="color:var(--error)">Ошибка загрузки раздела: ${escapeHtml(err.message)}</p>`;
  }
}

function buildSidebar() {
  const sidebar = document.getElementById('sidebar');
  const title = el('h1', {}, 'Заселение и аренда');
  sidebar.appendChild(title);
  for (const [route, label] of NAV_ITEMS) {
    const a = el('a', { class: 'nav-item', href: `#${route}`, 'data-route': route }, label);
    sidebar.appendChild(a);
  }
  const themeBtn = el('button', { class: 'theme-toggle', id: 'theme-toggle' }, '');
  themeBtn.addEventListener('click', () => {
    applyTheme(document.documentElement.getAttribute('data-theme') !== 'dark');
  });
  sidebar.appendChild(themeBtn);
}

window.addEventListener('hashchange', renderRoute);
window.addEventListener('DOMContentLoaded', () => {
  initTheme();
  buildSidebar();
  renderRoute();
});
