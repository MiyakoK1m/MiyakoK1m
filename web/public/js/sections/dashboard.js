Sections['dashboard'] = {
  async render(main) {
    const [trips, rentals] = await Promise.all([Api.list('trip-entries'), Api.list('rental-records')]);
    const today = todayAlmaty();
    const isDark = document.documentElement.getAttribute('data-theme') === 'dark';

    const rows = [];
    for (const trip of trips) {
      if (!isActiveToday(trip.checkinDate, trip.checkoutDate, today)) continue;
      for (const name of trip.guestNames.filter((n) => n.trim())) {
        const rental = rentals.find((r) => sameName(r.fullName, name) && rangesOverlap(trip.checkinDate, trip.checkoutDate, r.rentStartDate, r.rentEndDate));
        rows.push({
          name, department: trip.department, city: trip.city,
          rooms: rental ? rental.roomsCount : null, checkin: trip.checkinDate, checkout: trip.checkoutDate,
        });
      }
    }

    main.innerHTML = '';
    main.appendChild(el('h2', {}, 'Дэшборд'));

    const cities = [...new Set(rows.map((r) => r.city).filter(Boolean))];
    main.appendChild(el('p', { class: 'small-muted' }, `Сейчас проживает: ${rows.length} чел. в ${cities.length} городах`));

    main.appendChild(el('h3', {}, 'Кто сейчас проживает и в каком городе'));
    const tableWrap = el('div', { class: 'table-scroll card' });
    const table = el('table');
    table.appendChild(el('tr', {}, ['ФИО', 'Департамент', 'Город', 'Комнат', 'Заселён с', 'Выселяется'].map((h) => el('th', {}, h))));
    for (const row of rows) {
      const isCheckoutToday = row.checkout === today;
      table.appendChild(el('tr', {}, [
        el('td', {}, row.name),
        el('td', {}, row.department),
        el('td', {}, row.city),
        el('td', {}, row.rooms == null ? '—' : String(row.rooms)),
        el('td', {}, formatDate(row.checkin)),
        el('td', { style: isCheckoutToday ? 'color:var(--error);font-weight:600' : '' }, (isCheckoutToday ? '⚠ ' : '') + formatDate(row.checkout)),
      ]));
    }
    tableWrap.appendChild(table);
    main.appendChild(tableWrap);

    main.appendChild(el('h3', {}, 'Тепловая карта загруженности по городам'));
    const byCity = {};
    for (const row of rows) { if (row.city) byCity[row.city] = (byCity[row.city] || 0) + 1; }
    const maxCount = Math.max(0, ...Object.values(byCity));
    const heat = el('div', { class: 'heatmap' });
    const lightSteps = ['#e0e0e0', '#b8c9de', '#7fa3c9', '#4472a8', '#1e3a5f'];
    const darkSteps = ['#3a3a3a', '#32476b', '#2c5a8a', '#2e76b6', '#5fa8e8'];
    const steps = isDark ? darkSteps : lightSteps;
    Object.entries(byCity).sort((a, b) => b[1] - a[1]).forEach(([city, count]) => {
      const step = maxCount === 0 ? 0 : Math.min(4, Math.round((count / maxCount) * 4));
      const tile = el('div', { class: 'heat-tile', style: `background:${steps[step]}` });
      tile.appendChild(el('div', {}, city));
      tile.appendChild(el('div', { style: 'font-size:24px;font-weight:700' }, String(count)));
      heat.appendChild(tile);
    });
    main.appendChild(heat);

    const legend = el('div', { class: 'legend' });
    legend.appendChild(el('span', { class: 'small-muted' }, 'Меньше'));
    steps.forEach((c) => legend.appendChild(el('span', { class: 'swatch', style: `background:${c}` })));
    legend.appendChild(el('span', { class: 'small-muted' }, 'Больше'));
    main.appendChild(legend);
  },
};
