Sections['booking'] = {
  async render(main) {
    const [shared, templates, cities] = await Promise.all([Api.getSharedFields(), Api.getTemplates(), Api.list('cities')]);
    let template = templates.BOOKING;

    main.innerHTML = '';
    main.appendChild(el('h2', {}, 'Бронирование'));
    const card = el('div', { class: 'card' });
    main.appendChild(card);

    card.appendChild(el('h3', {}, 'Гости'));
    const namesContainer = el('div');
    card.appendChild(namesContainer);
    function renderNames() {
      namesContainer.innerHTML = '';
      shared.guestNames.forEach((name, idx) => {
        const row = el('div', { class: 'row', style: 'align-items:center' });
        const input = el('input', { type: 'text', value: name });
        input.addEventListener('change', async () => { shared.guestNames[idx] = input.value; await Api.updateSharedFields({ guestNames: shared.guestNames }); recompute(); });
        row.appendChild(input);
        if (shared.guestNames.length > 1) {
          const del = el('button', { class: 'secondary' }, '✕');
          del.addEventListener('click', async () => { shared.guestNames.splice(idx, 1); await Api.updateSharedFields({ guestNames: shared.guestNames }); renderNames(); recompute(); });
          row.appendChild(del);
        }
        namesContainer.appendChild(row);
      });
      const addBtn = el('button', { class: 'secondary' }, '+ Добавить ещё одного');
      addBtn.addEventListener('click', async () => { shared.guestNames.push(''); await Api.updateSharedFields({ guestNames: shared.guestNames }); renderNames(); });
      namesContainer.appendChild(addBtn);
    }
    renderNames();

    const cityField = field('Город', datalistInput('city-suggest-b', cities.map((c) => c.name), shared.city));
    card.appendChild(cityField);
    const cityInput = cityField.querySelector('input');
    cityInput.addEventListener('change', async () => { shared.city = cityInput.value; await Api.updateSharedFields({ city: shared.city }); recompute(); });

    const complexInput = el('input', { type: 'text', value: shared.complexName });
    card.appendChild(field('ЖК', complexInput));
    const addressInput = el('input', { type: 'text', value: shared.address });
    card.appendChild(field('Адрес', addressInput));

    card.appendChild(el('h3', {}, 'Тип квартиры'));
    const apartmentTiles = tilePicker(APARTMENT_TYPES.map(([c, l]) => [c, l]), shared.apartmentTypeName, async (v) => {
      shared.apartmentTypeName = v;
      await Api.updateSharedFields({ apartmentTypeName: v });
      otherField.style.display = v === 'OTHER' ? '' : 'none';
      recompute();
    });
    card.appendChild(apartmentTiles);
    const otherInput = el('input', { type: 'text', value: shared.apartmentTypeOtherText });
    const otherField = field('Тип квартиры (другое)', otherInput);
    otherField.style.display = shared.apartmentTypeName === 'OTHER' ? '' : 'none';
    otherInput.addEventListener('change', async () => { shared.apartmentTypeOtherText = otherInput.value; await Api.updateSharedFields({ apartmentTypeOtherText: otherInput.value }); recompute(); });
    card.appendChild(otherField);

    const dateRow = el('div', { class: 'row' });
    const checkinDate = el('input', { type: 'date', value: shared.checkinDateIso || '' });
    const checkoutDate = el('input', { type: 'date', value: shared.checkoutDateIso || '' });
    dateRow.appendChild(field('Дата заезда', checkinDate));
    dateRow.appendChild(field('Дата выезда', checkoutDate));
    card.appendChild(dateRow);

    const timeRow = el('div', { class: 'row' });
    const checkinTime = el('input', { type: 'text', value: shared.checkinTime });
    const checkoutTime = el('input', { type: 'text', value: shared.checkoutTime });
    timeRow.appendChild(field('Время заезда', checkinTime));
    timeRow.appendChild(field('Время выезда', checkoutTime));
    card.appendChild(timeRow);

    const rateInput = moneyInput(shared.dailyRate, async (v) => { shared.dailyRate = v; await Api.updateSharedFields({ dailyRate: v }); recompute(); });
    card.appendChild(field('Стоимость за сутки', rateInput));
    const depositInput = moneyInput(shared.deposit, async (v) => { shared.deposit = v; await Api.updateSharedFields({ deposit: v }); recompute(); });
    card.appendChild(field('Депозит', depositInput));

    const totalText = el('p', { style: 'font-weight:600' }, '');
    card.appendChild(totalText);

    checkinDate.addEventListener('change', async () => { shared.checkinDateIso = checkinDate.value || null; await Api.updateSharedFields({ checkinDateIso: shared.checkinDateIso }); recompute(); });
    checkoutDate.addEventListener('change', async () => { shared.checkoutDateIso = checkoutDate.value || null; await Api.updateSharedFields({ checkoutDateIso: shared.checkoutDateIso }); recompute(); });
    checkinTime.addEventListener('change', async () => { shared.checkinTime = checkinTime.value; await Api.updateSharedFields({ checkinTime: shared.checkinTime }); recompute(); });
    checkoutTime.addEventListener('change', async () => { shared.checkoutTime = checkoutTime.value; await Api.updateSharedFields({ checkoutTime: shared.checkoutTime }); recompute(); });
    complexInput.addEventListener('change', async () => { shared.complexName = complexInput.value; await Api.updateSharedFields({ complexName: shared.complexName }); recompute(); });
    addressInput.addEventListener('change', async () => { shared.address = addressInput.value; await Api.updateSharedFields({ address: shared.address }); recompute(); });

    main.appendChild(el('h3', {}, 'Шаблон (редактируемый)'));
    const templateArea = el('textarea', { style: 'width:100%;min-height:180px' }, template);
    main.appendChild(templateArea);
    templateArea.addEventListener('input', () => { Api.updateTemplate('BOOKING', templateArea.value); recompute(); });

    main.appendChild(el('h3', {}, 'Готовый текст'));
    const copyBox = el('div', { class: 'copy-box' });
    const output = el('textarea', { readonly: 'readonly', style: 'width:100%;min-height:200px' });
    copyBox.appendChild(output);
    const copyBtn = el('button', {}, 'Скопировать');
    copyBtn.addEventListener('click', () => navigator.clipboard.writeText(output.value));
    copyBox.appendChild(copyBtn);
    main.appendChild(copyBox);

    function recompute() {
      const nights = daysBetween(shared.checkinDateIso, shared.checkoutDateIso);
      const total = (shared.deposit || 0) + (shared.dailyRate || 0) * nights;
      totalText.textContent = `Итого: ${formatMoney(total)}`;
      const typeLabel = shared.apartmentTypeName === 'OTHER' ? shared.apartmentTypeOtherText : apartmentTypeLabel(shared.apartmentTypeName);
      const tokens = {
        'фио_гостей': shared.guestNames.filter((n) => n.trim()).join(', '), 'город': shared.city,
        'жк': shared.complexName, 'адрес': shared.address, 'тип_квартиры': typeLabel,
        'дата_заезда': formatDate(shared.checkinDateIso), 'дата_выезда': formatDate(shared.checkoutDateIso),
        'время_заезда': shared.checkinTime, 'время_выезда': shared.checkoutTime,
        'ставка': formatMoney(shared.dailyRate), 'депозит': formatMoney(shared.deposit), 'итого': formatMoney(total),
      };
      output.value = renderTemplate(templateArea.value, tokens);
    }
    recompute();
  },
};
