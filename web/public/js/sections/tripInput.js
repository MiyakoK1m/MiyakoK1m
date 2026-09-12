Sections['trip-input'] = {
  async render(main) {
    const [shared, cities, residents, trips, landlordsAll] = await Promise.all([
      Api.getSharedFields(), Api.list('cities'), Api.list('residents'), Api.list('trip-entries'), Api.list('landlords'),
    ]);

    const local = JSON.parse(sessionStorage.getItem('trip-input-local') || 'null') || {
      position: '', department: DEPARTMENTS[0], housingType: 'APARTMENT', offRegulation: false,
      perDiem: 0, urgencyReason: '', justification: '', purposeType: 'WAREHOUSE_OPENING', purposeOtherText: '',
    };
    const saveLocal = () => sessionStorage.setItem('trip-input-local', JSON.stringify(local));

    main.innerHTML = '';
    main.appendChild(el('h2', {}, 'Входная информация'));

    const card = el('div', { class: 'card' });
    main.appendChild(card);

    // ФИО проживающих
    card.appendChild(el('h3', {}, 'ФИО проживающих'));
    const namesContainer = el('div');
    card.appendChild(namesContainer);
    function renderNames() {
      namesContainer.innerHTML = '';
      shared.guestNames.forEach((name, idx) => {
        const row = el('div', { class: 'row', style: 'align-items:center' });
        const input = el('input', { type: 'text', value: name, placeholder: `ФИО ${idx + 1}` });
        input.addEventListener('change', async () => {
          shared.guestNames[idx] = input.value;
          await Api.updateSharedFields({ guestNames: shared.guestNames });
          renderWarnings();
        });
        row.appendChild(input);
        if (shared.guestNames.length > 1) {
          const del = el('button', { class: 'secondary' }, '✕');
          del.addEventListener('click', async () => {
            shared.guestNames.splice(idx, 1);
            await Api.updateSharedFields({ guestNames: shared.guestNames });
            renderNames();
            renderWarnings();
          });
          row.appendChild(del);
        }
        namesContainer.appendChild(row);
      });
      const addBtn = el('button', { class: 'secondary' }, '+ Добавить ещё одного');
      addBtn.addEventListener('click', async () => {
        shared.guestNames.push('');
        await Api.updateSharedFields({ guestNames: shared.guestNames });
        renderNames();
      });
      namesContainer.appendChild(addBtn);
    }
    renderNames();

    // Должность
    card.appendChild(field('Должность', el('input', {
      type: 'text', value: local.position, oninput: (e) => { local.position = e.target.value; saveLocal(); },
    })));

    // Департамент
    card.appendChild(field('Департамент', selectEl(DEPARTMENTS, local.department, (v) => { local.department = v; saveLocal(); })));

    // Вид жилья
    card.appendChild(el('h3', {}, 'Вид жилья'));
    card.appendChild(tilePicker([['APARTMENT', 'Квартира'], ['HOTEL', 'Отель']], local.housingType, (v) => { local.housingType = v; saveLocal(); }));

    // Вне регламента
    card.appendChild(el('h3', { style: 'margin-top:16px' }, 'Командировка вне регламента'));
    card.appendChild(tilePicker([['false', 'Нет'], ['true', 'Да']], String(local.offRegulation), (v) => { local.offRegulation = v === 'true'; saveLocal(); }, (v) => v === 'true'));

    // Город
    const cityField = field('Город назначения', datalistInput('city-suggest', cities.map((c) => c.name), shared.city));
    card.appendChild(cityField);
    const cityInput = cityField.querySelector('input');
    cityInput.addEventListener('change', async () => {
      shared.city = cityInput.value;
      await Api.updateSharedFields({ city: shared.city });
      renderWarnings();
      renderLandlords();
    });

    // Даты
    const dateRow = el('div', { class: 'row' });
    const checkinInput = el('input', { type: 'date', value: shared.checkinDateIso || '' });
    const checkoutInput = el('input', { type: 'date', value: shared.checkoutDateIso || '' });
    checkinInput.addEventListener('change', async () => { shared.checkinDateIso = checkinInput.value || null; await Api.updateSharedFields({ checkinDateIso: shared.checkinDateIso }); });
    checkoutInput.addEventListener('change', async () => { shared.checkoutDateIso = checkoutInput.value || null; await Api.updateSharedFields({ checkoutDateIso: shared.checkoutDateIso }); });
    dateRow.appendChild(field('Дата заселения', checkinInput));
    dateRow.appendChild(field('Дата выселения', checkoutInput));
    card.appendChild(dateRow);

    // Сумма суточных
    card.appendChild(field('Сумма суточных, ₸', el('input', {
      type: 'number', value: local.perDiem || '', oninput: (e) => { local.perDiem = Number(e.target.value) || 0; saveLocal(); },
    })));

    card.appendChild(field('Причина срочности', el('input', {
      type: 'text', value: local.urgencyReason, oninput: (e) => { local.urgencyReason = e.target.value; saveLocal(); },
    })));

    card.appendChild(field('Обоснование командировки', el('textarea', {
      oninput: (e) => { local.justification = e.target.value; saveLocal(); },
    }, local.justification)));

    const purposeCard = el('div', { class: 'card' });
    purposeCard.appendChild(el('h3', {}, 'Цель поездки'));
    purposeCard.appendChild(tilePicker([['WAREHOUSE_OPENING', 'В рамках открытия складов'], ['OTHER', 'Другое']], local.purposeType, (v) => { local.purposeType = v; saveLocal(); }));
    main.appendChild(purposeCard);

    const purposeOtherCard = el('div', { class: 'card' });
    purposeOtherCard.appendChild(el('h3', {}, 'Цель поездки (другое)'));
    purposeOtherCard.appendChild(el('textarea', {
      placeholder: 'Развёрнутая альтернативная формулировка, если нужна',
      oninput: (e) => { local.purposeOtherText = e.target.value; saveLocal(); },
    }, local.purposeOtherText));
    main.appendChild(purposeOtherCard);

    const warningsHost = el('div');
    main.appendChild(warningsHost);
    function renderWarnings() {
      warningsHost.innerHTML = '';
      const residentWarnings = [];
      for (const name of shared.guestNames.filter((n) => n.trim())) {
        const match = residents.find((r) => sameName(r.fullName, name));
        if (match) residentWarnings.push(`⚠ ${match.fullName} — ${match.comment}`);
      }
      if (residentWarnings.length) {
        const panel = el('div', { class: 'warning-panel' });
        panel.appendChild(el('strong', {}, 'Проверка по списку "Проживающие сотрудники"'));
        residentWarnings.forEach((w) => panel.appendChild(el('div', {}, w)));
        warningsHost.appendChild(panel);
      }
      const historyWarnings = [];
      if (shared.city.trim()) {
        for (const name of shared.guestNames.filter((n) => n.trim())) {
          for (const trip of trips) {
            if (trip.city?.toLowerCase() === shared.city.toLowerCase() && trip.guestNames.some((n) => sameName(n, name))) {
              historyWarnings.push(`⚠ ${name} уже был(а) в командировке в этом городе: ${formatDate(trip.checkinDate)} – ${formatDate(trip.checkoutDate)} — «${trip.justification}»`);
            }
          }
        }
      }
      if (historyWarnings.length) {
        const panel = el('div', { class: 'warning-panel' });
        panel.appendChild(el('strong', {}, 'История командировок в этот город'));
        historyWarnings.forEach((w) => panel.appendChild(el('div', {}, w)));
        warningsHost.appendChild(panel);
      }
    }
    renderWarnings();

    const landlordsHost = el('div');
    main.appendChild(landlordsHost);
    function renderLandlords() {
      landlordsHost.innerHTML = '';
      if (!shared.city.trim()) return;
      const inCity = landlordsAll.filter((l) => l.city.toLowerCase() === shared.city.toLowerCase())
        .sort((a, b) => b.rating - a.rating);
      if (!inCity.length) return;
      landlordsHost.appendChild(el('h3', {}, 'Арендодатели в этом городе'));
      for (const l of inCity) {
        const color = l.rating >= 7 ? '#2e7d32' : l.rating >= 4 ? '#f9a825' : '#c62828';
        const card2 = el('div', { class: 'card', style: 'display:flex;justify-content:space-between;align-items:center' });
        const info = el('div');
        info.appendChild(el('div', {}, l.fullName));
        info.appendChild(el('div', { class: 'small-muted' }, `${l.phone} · ${l.comment || ''}`));
        card2.appendChild(info);
        const right = el('div', { style: 'text-align:right' });
        right.appendChild(el('span', { class: 'badge-rating', style: `background:${color}` }, `★ ${l.rating}`));
        const waLink = el('div', { style: 'margin-top:8px' });
        const names = shared.guestNames.filter((n) => n.trim()).join(', ');
        const message = `Здравствуйте! По поводу аренды в г. ${shared.city} для ${names}, заезд ${formatDate(shared.checkinDateIso)}, выезд ${formatDate(shared.checkoutDateIso)}.`;
        const phoneDigits = (l.phone || '').replace(/\D/g, '');
        const a = el('a', { class: 'btn', href: `https://wa.me/${phoneDigits}?text=${encodeURIComponent(message)}`, target: '_blank' }, 'WhatsApp');
        waLink.appendChild(a);
        right.appendChild(waLink);
        card2.appendChild(right);
        landlordsHost.appendChild(card2);
      }
    }
    renderLandlords();

    const saveBtn = el('button', { style: 'margin-top:12px' }, 'Сохранить командировку');
    const savedMsg = el('span', { class: 'small-muted', style: 'margin-left:12px' }, '');
    saveBtn.addEventListener('click', async () => {
      await Api.create('trip-entries', {
        guestNames: shared.guestNames, position: local.position, department: local.department,
        housingType: local.housingType, offRegulation: local.offRegulation, city: shared.city,
        checkinDate: shared.checkinDateIso, checkoutDate: shared.checkoutDateIso, perDiem: local.perDiem,
        urgencyReason: local.urgencyReason, justification: local.justification,
        purposeType: local.purposeType, purposeOtherText: local.purposeOtherText,
        createdAt: Date.now(),
      });
      savedMsg.textContent = `Сохранено: ${formatDate(todayAlmaty())}`;
    });
    main.appendChild(saveBtn);
    main.appendChild(savedMsg);
  },
};

