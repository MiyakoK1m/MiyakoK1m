Sections['calendar'] = {
  async render(main) {
    const [trips, rentals] = await Promise.all([Api.list('trip-entries'), Api.list('rental-records')]);
    const today = new Date(todayAlmaty());
    let viewYear = today.getFullYear();
    let viewMonth = today.getMonth(); // 0-based

    main.innerHTML = '';
    main.appendChild(el('h2', {}, 'Календарь'));

    const nav = el('div', { style: 'display:flex;align-items:center;justify-content:space-between;max-width:400px' });
    const prevBtn = el('button', { class: 'secondary' }, '‹');
    const monthLabel = el('strong', {}, '');
    const nextBtn = el('button', { class: 'secondary' }, '›');
    nav.appendChild(prevBtn); nav.appendChild(monthLabel); nav.appendChild(nextBtn);
    main.appendChild(nav);
    const todayBtn = el('button', { class: 'secondary', style: 'margin:8px 0' }, 'Сегодня');
    main.appendChild(todayBtn);

    const weekdayRow = el('div', { class: 'calendar-grid' });
    ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'].forEach((d) => weekdayRow.appendChild(el('div', { class: 'weekday-header' }, d)));
    main.appendChild(weekdayRow);

    const grid = el('div', { class: 'calendar-grid', style: 'margin-top:4px' });
    main.appendChild(grid);

    const monthNames = ['Январь', 'Февраль', 'Март', 'Апрель', 'Май', 'Июнь', 'Июль', 'Август', 'Сентябрь', 'Октябрь', 'Ноябрь', 'Декабрь'];

    function isoOf(y, m, d) {
      return `${y}-${String(m + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
    }

    function badgesForDay(dayIso) {
      const badges = [];
      const namesFromTrips = new Set();
      for (const trip of trips) {
        if (!isActiveToday(trip.checkinDate, trip.checkoutDate, dayIso)) continue;
        for (const name of trip.guestNames.filter((n) => n.trim())) {
          namesFromTrips.add(name.toLowerCase());
          const rental = rentals.find((r) => sameName(r.fullName, name) && isActiveToday(r.rentStartDate, r.rentEndDate, dayIso));
          badges.push({
            name, source: rental ? 'TRIP_WITH_RENTAL' : 'TRIP_ONLY', isCheckout: dayIso === trip.checkoutDate,
            trip, rental,
          });
        }
      }
      for (const rental of rentals) {
        if (!rental.fullName || !isActiveToday(rental.rentStartDate, rental.rentEndDate, dayIso)) continue;
        const already = [...namesFromTrips].some((n) => sameName(n, rental.fullName));
        if (already) continue;
        badges.push({ name: rental.fullName, source: 'RENTAL_ONLY', isCheckout: dayIso === rental.rentEndDate, trip: null, rental });
      }
      return badges;
    }

    function showDetail(badge) {
      const backdrop = el('div', { class: 'modal-backdrop' });
      const modal = el('div', { class: 'modal' });
      modal.appendChild(el('h3', {}, badge.name));
      if (badge.trip) {
        modal.appendChild(el('div', {}, `Командировка: ${formatDate(badge.trip.checkinDate)} – ${formatDate(badge.trip.checkoutDate)}`));
        modal.appendChild(el('div', {}, `Город: ${badge.trip.city}, Департамент: ${badge.trip.department}`));
      }
      if (badge.rental) {
        modal.appendChild(el('div', { style: 'margin-top:8px' }, `Аренда: ${formatDate(badge.rental.rentStartDate)} – ${formatDate(badge.rental.rentEndDate)}`));
        modal.appendChild(el('div', {}, `Адрес: ${badge.rental.address}`));
        modal.appendChild(el('div', {}, `Тип квартиры: ${apartmentTypeLabel(badge.rental.apartmentType)}`));
        modal.appendChild(el('div', {}, `Ставка: ${formatMoney(badge.rental.rentAmount)}`));
        modal.appendChild(el('div', {}, `Депозит: ${formatMoney(badge.rental.depositAmount)}`));
      }
      if (badge.isCheckout) {
        modal.appendChild(el('div', { style: 'color:var(--error);margin-top:8px' }, '⚠ Сегодня дата выселения — напоминание вернуть депозит'));
      }
      const closeBtn = el('button', { style: 'margin-top:14px' }, 'Закрыть');
      closeBtn.addEventListener('click', () => document.body.removeChild(backdrop));
      modal.appendChild(closeBtn);
      backdrop.appendChild(modal);
      document.body.appendChild(backdrop);
    }

    function renderMonth() {
      monthLabel.textContent = `${monthNames[viewMonth]} ${viewYear}`;
      grid.innerHTML = '';
      const firstDay = new Date(viewYear, viewMonth, 1);
      const leading = (firstDay.getDay() + 6) % 7; // Monday-first
      const daysInMonth = new Date(viewYear, viewMonth + 1, 0).getDate();
      for (let i = 0; i < leading; i++) grid.appendChild(el('div'));
      for (let d = 1; d <= daysInMonth; d++) {
        const dayIso = isoOf(viewYear, viewMonth, d);
        const cell = el('div', { class: 'calendar-cell' + (dayIso === todayAlmaty() ? ' today' : '') });
        cell.appendChild(el('div', {}, String(d)));
        const badges = badgesForDay(dayIso).slice(0, 4);
        for (const badge of badges) {
          const bg = badge.isCheckout ? 'var(--error)' : badge.source === 'RENTAL_ONLY' ? '#2e7d32' : 'var(--primary)';
          const icon = badge.source === 'TRIP_WITH_RENTAL' ? ' 🏠' : badge.source === 'RENTAL_ONLY' ? ' 🔑' : '';
          const b = el('div', { class: 'day-badge', style: `background:${bg}` }, badge.name + icon);
          b.addEventListener('click', () => showDetail(badge));
          cell.appendChild(b);
        }
        grid.appendChild(cell);
      }
    }

    prevBtn.addEventListener('click', () => { viewMonth--; if (viewMonth < 0) { viewMonth = 11; viewYear--; } renderMonth(); });
    nextBtn.addEventListener('click', () => { viewMonth++; if (viewMonth > 11) { viewMonth = 0; viewYear++; } renderMonth(); });
    todayBtn.addEventListener('click', () => { viewYear = today.getFullYear(); viewMonth = today.getMonth(); renderMonth(); });

    renderMonth();
  },
};
