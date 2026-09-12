Sections['finance-approval'] = {
  async render(main) {
    const [shared, templates, cities, registry] = await Promise.all([
      Api.getSharedFields(), Api.getTemplates(), Api.list('cities'), Api.list('finance-registry'),
    ]);
    let template = templates.FINANCE_OBJECT;
    let extraObjects = [];
    let nextLocalId = 1;
    let checkedIds = new Set();

    main.innerHTML = '';
    main.appendChild(el('h2', {}, 'Согласование с финансами'));

    const mainCard = el('div', { class: 'card' });
    main.appendChild(mainCard);
    mainCard.appendChild(el('h3', {}, 'Основной объект'));

    const cityField = field('Город', datalistInput('city-suggest-f', cities.map((c) => c.name), shared.city));
    mainCard.appendChild(cityField);
    const cityInput = cityField.querySelector('input');
    cityInput.addEventListener('change', async () => { shared.city = cityInput.value; await Api.updateSharedFields({ city: shared.city }); recompute(); });

    const fioText = el('p', {}, '');
    mainCard.appendChild(fioText);

    const apartmentTiles = tilePicker(APARTMENT_TYPES.map(([c, l]) => [c, l]), shared.apartmentTypeName, async (v) => {
      shared.apartmentTypeName = v; await Api.updateSharedFields({ apartmentTypeName: v }); recompute();
    });
    mainCard.appendChild(apartmentTiles);

    const guestsText = el('p', {}, '');
    mainCard.appendChild(guestsText);

    const dateRow = el('div', { class: 'row' });
    const checkinDate = el('input', { type: 'date', value: shared.checkinDateIso || '' });
    const checkoutDate = el('input', { type: 'date', value: shared.checkoutDateIso || '' });
    dateRow.appendChild(field('Заезд', checkinDate));
    dateRow.appendChild(field('Выезд', checkoutDate));
    mainCard.appendChild(dateRow);
    checkinDate.addEventListener('change', async () => { shared.checkinDateIso = checkinDate.value || null; await Api.updateSharedFields({ checkinDateIso: shared.checkinDateIso }); recompute(); });
    checkoutDate.addEventListener('change', async () => { shared.checkoutDateIso = checkoutDate.value || null; await Api.updateSharedFields({ checkoutDateIso: shared.checkoutDateIso }); recompute(); });

    const rateInput = moneyInput(shared.dailyRate, async (v) => { shared.dailyRate = v; await Api.updateSharedFields({ dailyRate: v }); recompute(); });
    mainCard.appendChild(field('Стоимость за сутки', rateInput));
    const depositInput = moneyInput(shared.deposit, async (v) => { shared.deposit = v; await Api.updateSharedFields({ deposit: v }); recompute(); });
    mainCard.appendChild(field('Депозит', depositInput));
    const mainTotalText = el('p', { style: 'font-weight:600' }, '');
    mainCard.appendChild(mainTotalText);

    const extrasHost = el('div');
    main.appendChild(extrasHost);

    const addObjectBtn = el('button', { class: 'secondary' }, '+ Добавить объект');
    main.appendChild(addObjectBtn);

    main.appendChild(el('h3', {}, 'Шаблон объекта (редактируемый)'));
    const templateArea = el('textarea', { style: 'width:100%;min-height:160px' }, template);
    main.appendChild(templateArea);

    main.appendChild(el('h3', {}, 'Готовый текст'));
    const copyBox = el('div', { class: 'copy-box' });
    const output = el('textarea', { readonly: 'readonly', style: 'width:100%;min-height:220px' });
    copyBox.appendChild(output);
    const copyBtn = el('button', {}, 'Скопировать');
    copyBtn.addEventListener('click', () => navigator.clipboard.writeText(output.value));
    copyBox.appendChild(copyBtn);
    main.appendChild(copyBox);

    const saveRegistryBtn = el('button', { style: 'margin-top:16px' }, 'Сохранить в реестр');
    main.appendChild(saveRegistryBtn);

    main.appendChild(el('h3', { style: 'margin-top:24px' }, 'Реестр согласований с финансами'));
    const registryHost = el('div');
    main.appendChild(registryHost);
    const registryActions = el('div', { class: 'pill-actions' });
    const addSelectedBtn = el('button', {}, '➕ Добавить выбранные как объекты');
    const exportBtn = el('button', { class: 'secondary' }, 'Скачать Excel');
    registryActions.appendChild(addSelectedBtn);
    registryActions.appendChild(exportBtn);
    main.appendChild(registryActions);

    function renderExtraObjects() {
      extrasHost.innerHTML = '';
      for (const obj of extraObjects) {
        extrasHost.appendChild(renderExtraObjectCard(obj));
      }
    }

    function objTotal(obj) {
      const nights = daysBetween(obj.checkinDate, obj.checkoutDate);
      return (obj.deposit || 0) + (obj.dailyRate || 0) * nights;
    }

    function renderExtraObjectCard(obj) {
      const card = el('div', { class: 'card' });
      const header = el('div', { style: 'display:flex;justify-content:space-between' });
      header.appendChild(el('h3', {}, 'Дополнительный объект'));
      const removeBtn = el('button', { class: 'secondary' }, '✕');
      removeBtn.addEventListener('click', () => { extraObjects = extraObjects.filter((o) => o.localId !== obj.localId); renderExtraObjects(); recompute(); });
      header.appendChild(removeBtn);
      card.appendChild(header);

      const cityF = field('Город', datalistInput(`city-suggest-obj-${obj.localId}`, cities.map((c) => c.name), obj.city));
      const cityI = cityF.querySelector('input');
      cityI.addEventListener('change', () => { obj.city = cityI.value; recompute(); });
      card.appendChild(cityF);

      const fioInput = el('input', { type: 'text', value: obj.fullName });
      fioInput.addEventListener('change', () => { obj.fullName = fioInput.value; recompute(); });
      card.appendChild(field('ФИО', fioInput));

      card.appendChild(tilePicker(APARTMENT_TYPES.map(([c, l]) => [c, l]), obj.apartmentType, (v) => { obj.apartmentType = v; recompute(); }));

      const guestsInput = el('input', { type: 'number', value: obj.guestsCount || '' });
      guestsInput.addEventListener('change', () => { obj.guestsCount = Number(guestsInput.value) || 0; recompute(); });
      card.appendChild(field('Количество гостей', guestsInput));

      const dRow = el('div', { class: 'row' });
      const inDate = el('input', { type: 'date', value: obj.checkinDate || '' });
      const outDate = el('input', { type: 'date', value: obj.checkoutDate || '' });
      inDate.addEventListener('change', () => { obj.checkinDate = inDate.value || null; recompute(); });
      outDate.addEventListener('change', () => { obj.checkoutDate = outDate.value || null; recompute(); });
      dRow.appendChild(field('Заезд', inDate));
      dRow.appendChild(field('Выезд', outDate));
      card.appendChild(dRow);

      const rateI = moneyInput(obj.dailyRate, (v) => { obj.dailyRate = v; recompute(); });
      card.appendChild(field('Стоимость за сутки', rateI));
      const depI = moneyInput(obj.deposit, (v) => { obj.deposit = v; recompute(); });
      card.appendChild(field('Депозит', depI));

      const commentInput = el('input', { type: 'text', value: obj.comment });
      commentInput.addEventListener('change', () => { obj.comment = commentInput.value; recompute(); });
      card.appendChild(field('Комментарий', commentInput));

      card.appendChild(el('p', { style: 'font-weight:600' }, `Итого: ${formatMoney(objTotal(obj))}`));
      return card;
    }

    addObjectBtn.addEventListener('click', () => {
      extraObjects.push({
        localId: nextLocalId++, city: '', fullName: '', apartmentType: 'ONE_ROOM', guestsCount: 1,
        checkinDate: null, checkoutDate: null, dailyRate: 0, deposit: 0, comment: '',
      });
      renderExtraObjects();
      recompute();
    });

    function renderRegistry() {
      registryHost.innerHTML = '';
      if (!registry.length) { registryHost.appendChild(el('p', { class: 'small-muted' }, 'Реестр пуст.')); return; }
      const table = el('table');
      table.appendChild(el('tr', {}, ['', 'Город', 'ФИО', 'Заезд', 'Выезд', 'Итого', ''].map((h) => el('th', {}, h))));
      for (const entry of registry) {
        const checkbox = el('input', { type: 'checkbox' });
        checkbox.checked = checkedIds.has(entry.id);
        checkbox.addEventListener('change', () => { checkbox.checked ? checkedIds.add(entry.id) : checkedIds.delete(entry.id); });
        const delBtn = el('button', { class: 'secondary' }, '✕');
        delBtn.addEventListener('click', async () => { await Api.remove('finance-registry', entry.id); registry.splice(registry.indexOf(entry), 1); renderRegistry(); });
        table.appendChild(el('tr', {}, [
          el('td', {}, checkbox), el('td', {}, entry.city), el('td', {}, entry.fullName),
          el('td', {}, formatDate(entry.checkinDate)), el('td', {}, formatDate(entry.checkoutDate)),
          el('td', {}, formatMoney(entry.total)), el('td', {}, delBtn),
        ]));
      }
      registryHost.appendChild(el('div', { class: 'table-scroll' }, table));
    }

    saveRegistryBtn.addEventListener('click', async () => {
      const nights = daysBetween(shared.checkinDateIso, shared.checkoutDateIso);
      const mainEntry = await Api.create('finance-registry', {
        savedAt: Date.now(), city: shared.city, fullName: shared.guestNames.filter((n) => n.trim()).join(', '),
        apartmentType: shared.apartmentTypeName, apartmentTypeOtherText: shared.apartmentTypeOtherText,
        guestsCount: shared.guestNames.filter((n) => n.trim()).length || 1,
        checkinDate: shared.checkinDateIso, checkoutDate: shared.checkoutDateIso, nights,
        dailyRate: shared.dailyRate, deposit: shared.deposit, total: (shared.deposit || 0) + (shared.dailyRate || 0) * nights, comment: '',
      });
      registry.unshift(mainEntry);
      for (const obj of extraObjects) {
        const entry = await Api.create('finance-registry', {
          savedAt: Date.now(), city: obj.city, fullName: obj.fullName, apartmentType: obj.apartmentType,
          apartmentTypeOtherText: obj.apartmentTypeOtherText || '', guestsCount: obj.guestsCount,
          checkinDate: obj.checkinDate, checkoutDate: obj.checkoutDate, nights: daysBetween(obj.checkinDate, obj.checkoutDate),
          dailyRate: obj.dailyRate, deposit: obj.deposit, total: objTotal(obj), comment: obj.comment,
        });
        registry.unshift(entry);
      }
      renderRegistry();
    });

    addSelectedBtn.addEventListener('click', async () => {
      const checked = registry.filter((r) => checkedIds.has(r.id));
      if (!checked.length) return;
      let remaining = checked;
      if (!shared.city.trim() && shared.guestNames.every((n) => !n.trim())) {
        const first = checked[0];
        shared.city = first.city;
        shared.guestNames = first.fullName.split(',').map((s) => s.trim()).filter(Boolean);
        if (!shared.guestNames.length) shared.guestNames = [''];
        shared.apartmentTypeName = first.apartmentType;
        shared.checkinDateIso = first.checkinDate; shared.checkoutDateIso = first.checkoutDate;
        shared.dailyRate = first.dailyRate; shared.deposit = first.deposit;
        await Api.updateSharedFields(shared);
        remaining = checked.slice(1);
      }
      for (const entry of remaining) {
        extraObjects.push({
          localId: nextLocalId++, city: entry.city, fullName: entry.fullName, apartmentType: entry.apartmentType,
          guestsCount: entry.guestsCount, checkinDate: entry.checkinDate, checkoutDate: entry.checkoutDate,
          dailyRate: entry.dailyRate, deposit: entry.deposit, comment: entry.comment,
        });
      }
      checkedIds = new Set();
      await Sections['finance-approval'].render(main);
    });

    exportBtn.addEventListener('click', () => {
      downloadXlsx('Реестр согласований.xlsx', [{
        name: 'Реестр',
        headers: ['Дата сохранения', 'Город', 'ФИО', 'Тип квартиры', 'Гостей', 'Заезд', 'Выезд', 'Суток', 'Ставка', 'Депозит', 'Итого', 'Комментарий'],
        rows: registry.map((r) => [
          formatDate(new Date(r.savedAt).toISOString().slice(0, 10)), r.city, r.fullName,
          r.apartmentType === 'OTHER' ? r.apartmentTypeOtherText : apartmentTypeLabel(r.apartmentType),
          r.guestsCount, formatDate(r.checkinDate), formatDate(r.checkoutDate), r.nights, r.dailyRate, r.deposit, r.total, r.comment,
        ]),
      }]);
    });

    function recompute() {
      const guestsCount = shared.guestNames.filter((n) => n.trim()).length || 1;
      const nights = daysBetween(shared.checkinDateIso, shared.checkoutDateIso);
      const mainTotal = (shared.deposit || 0) + (shared.dailyRate || 0) * nights;
      fioText.textContent = `ФИО: ${shared.guestNames.filter((n) => n.trim()).join(', ') || '—'}`;
      guestsText.textContent = `Количество гостей (авто): ${guestsCount}`;
      mainTotalText.textContent = `Итого: ${formatMoney(mainTotal)}`;

      const mainTypeLabel = shared.apartmentTypeName === 'OTHER' ? shared.apartmentTypeOtherText : apartmentTypeLabel(shared.apartmentTypeName);
      let text = renderTemplate(templateArea.value, {
        'город': shared.city, 'фио': shared.guestNames.filter((n) => n.trim()).join(', '),
        'тип_квартиры': mainTypeLabel, 'гостей': String(guestsCount),
        'дата_заезда': formatDate(shared.checkinDateIso), 'дата_выезда': formatDate(shared.checkoutDateIso),
        'ставка': formatMoney(shared.dailyRate), 'депозит': formatMoney(shared.deposit), 'итого': formatMoney(mainTotal), 'комментарий': '',
      });
      for (const obj of extraObjects) {
        const typeLabel = obj.apartmentType === 'OTHER' ? obj.apartmentTypeOtherText : apartmentTypeLabel(obj.apartmentType);
        text += '\n\n———\n\n' + renderTemplate(templateArea.value, {
          'город': obj.city, 'фио': obj.fullName, 'тип_квартиры': typeLabel, 'гостей': String(obj.guestsCount),
          'дата_заезда': formatDate(obj.checkinDate), 'дата_выезда': formatDate(obj.checkoutDate),
          'ставка': formatMoney(obj.dailyRate), 'депозит': formatMoney(obj.deposit), 'итого': formatMoney(objTotal(obj)), 'комментарий': obj.comment,
        });
      }
      if (extraObjects.length) {
        const grand = mainTotal + extraObjects.reduce((sum, o) => sum + objTotal(o), 0);
        text += `\n\nИтоговая сумма: ${formatMoney(grand)}`;
      }
      output.value = text;
    }

    templateArea.addEventListener('input', () => { Api.updateTemplate('FINANCE_OBJECT', templateArea.value); recompute(); });

    renderExtraObjects();
    renderRegistry();
    recompute();
  },
};
