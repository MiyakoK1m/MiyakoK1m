const RENTAL_HEADERS = [
  'Код аэропорта', 'Город', 'Периодичность', 'Дата начала аренды', 'Дата завершения аренды',
  'Срок аренды (дней)', 'Сумма аренды', 'Итого сумма аренды', 'Дата оплаты аренды',
  'Сумма депозита', 'Статус возврата депозита', 'Департамент', 'ФИО', 'Цель поездки',
  'Цель поездки (другое)', 'Кол-во комнат', 'Гостей', 'Экономия 2-местное', 'Экономия 1-местное',
  'Адрес', 'Тип квартиры', 'Тип квартиры (другое)', 'Квартплата',
  'Арендодатель ФИО', 'Арендодатель телефон', 'Способ оплаты', 'Рейтинг арендодателя',
  'Файл чека', 'Описание чека',
  'ЖК', 'Подъезд', 'Примечание к подъезду', 'Этаж', 'Номер квартиры', 'Код домофона',
  'Время заселения', 'Время выселения', '2ГИС текст', '2ГИС ссылка', 'Wi-Fi имя', 'Wi-Fi пароль',
  'Комментарий',
];

function rentalRecordToRow(r) {
  return [
    r.airportCode, r.city, r.periodicity === 'DAILY' ? 'Ежедневно' : 'Ежемесячно',
    formatDate(r.rentStartDate), formatDate(r.rentEndDate), r.rentDurationDays, r.rentAmount, r.rentTotalAmount,
    formatDate(r.rentPaymentDate), r.depositAmount,
    { NONE: '', RETURNED: 'Вернули', WITH_LANDLORD: 'У арендатора' }[r.depositReturnStatus] || '',
    r.department, r.fullName, r.purposeType === 'OTHER' ? 'Другое' : 'В рамках открытия складов', r.purposeOtherText,
    r.roomsCount, r.guestsCount, r.savingsTwoPerson, r.savingsOnePerson, r.address,
    r.apartmentType === 'OTHER' ? apartmentTypeLabel('OTHER') : apartmentTypeLabel(r.apartmentType), r.apartmentTypeOtherText,
    r.utilitiesAmount, r.landlordFullName, r.landlordPhone, r.landlordPaymentMethod === 'INVOICE' ? 'Счёт' : 'Перевод',
    r.landlordRating, r.receiptFileName, r.receiptDescription, r.complexName, r.entrance, r.entranceNote,
    r.floor, r.apartmentNumber, r.intercomCode, r.checkinTimeFrom, r.checkoutTimeTo, r.twoGisLinkText,
    r.twoGisLinkUrl, r.wifiName, r.wifiPassword, r.additionalComment,
  ];
}

function rowToRentalRecord(headers, row) {
  const get = (name) => { const i = matchHeaderIndex(headers, name); return i == null ? '' : String(row[i] ?? '').trim(); };
  const apt = APARTMENT_TYPES.find(([, label]) => label.toLowerCase() === get('Тип квартиры').toLowerCase());
  return {
    airportCode: get('Код аэропорта'), city: get('Город'),
    periodicity: get('Периодичность').toLowerCase() === 'ежемесячно' ? 'MONTHLY' : 'DAILY',
    rentStartDate: parseDateFlexible(get('Дата начала аренды')), rentEndDate: parseDateFlexible(get('Дата завершения аренды')),
    rentDurationDays: Number(get('Срок аренды (дней)')) || 0, rentAmount: Number(get('Сумма аренды')) || 0,
    rentTotalAmount: Number(get('Итого сумма аренды')) || 0, rentPaymentDate: parseDateFlexible(get('Дата оплаты аренды')),
    depositAmount: Number(get('Сумма депозита')) || 0,
    depositReturnStatus: get('Статус возврата депозита').toLowerCase() === 'вернули' ? 'RETURNED'
      : get('Статус возврата депозита').toLowerCase() === 'у арендатора' ? 'WITH_LANDLORD' : 'NONE',
    department: DEPARTMENTS.find((d) => d.toLowerCase() === get('Департамент').toLowerCase()) || get('Департамент') || DEPARTMENTS[0],
    fullName: get('ФИО'), purposeType: get('Цель поездки').toLowerCase() === 'другое' ? 'OTHER' : 'WAREHOUSE_OPENING',
    purposeOtherText: get('Цель поездки (другое)'),
    roomsCount: Number(get('Кол-во комнат')) || 0, guestsCount: Number(get('Гостей')) || 1,
    savingsTwoPerson: Number(get('Экономия 2-местное')) || 0, savingsOnePerson: Number(get('Экономия 1-местное')) || 0,
    address: get('Адрес'), apartmentType: apt ? apt[0] : 'ONE_ROOM', apartmentTypeOtherText: get('Тип квартиры (другое)'),
    utilitiesAmount: Number(get('Квартплата')) || 0,
    landlordFullName: get('Арендодатель ФИО'), landlordPhone: get('Арендодатель телефон'),
    landlordPaymentMethod: get('Способ оплаты').toLowerCase() === 'счёт' ? 'INVOICE' : 'TRANSFER',
    landlordRating: Number(get('Рейтинг арендодателя')) || 7,
    receiptFileName: get('Файл чека'), receiptDescription: get('Описание чека'), receiptUri: '',
    complexName: get('ЖК'), entrance: get('Подъезд'), entranceNote: get('Примечание к подъезду'),
    floor: get('Этаж'), apartmentNumber: get('Номер квартиры'), intercomCode: get('Код домофона'),
    checkinTimeFrom: get('Время заселения'), checkoutTimeTo: get('Время выселения'),
    twoGisLinkText: get('2ГИС текст'), twoGisLinkUrl: get('2ГИС ссылка'),
    wifiName: get('Wi-Fi имя'), wifiPassword: get('Wi-Fi пароль'), additionalComment: get('Комментарий'),
  };
}

function freshRentalDraft(shared) {
  return {
    airportCode: '', city: shared.city, periodicity: 'DAILY', rentStartDate: shared.checkinDateIso, rentEndDate: shared.checkoutDateIso,
    rentDurationDays: 0, rentAmount: 0, rentTotalAmount: 0, rentPaymentDate: null, depositAmount: 0, depositReturnStatus: 'NONE',
    department: DEPARTMENTS[0], fullName: (shared.guestNames || []).find((n) => n.trim()) || '',
    purposeType: 'WAREHOUSE_OPENING', purposeOtherText: '', roomsCount: apartmentRoomsCount(shared.apartmentTypeName),
    guestsCount: 1, savingsTwoPerson: 0, savingsOnePerson: 0, address: shared.address,
    apartmentType: shared.apartmentTypeName, apartmentTypeOtherText: shared.apartmentTypeOtherText, utilitiesAmount: 0,
    landlordFullName: '', landlordPhone: '', landlordPaymentMethod: 'TRANSFER', landlordRating: 7,
    receiptFileName: '', receiptUri: '', receiptDescription: '',
    complexName: shared.complexName, entrance: '', entranceNote: '', floor: '', apartmentNumber: '', intercomCode: '',
    checkinTimeFrom: shared.checkinTime, checkoutTimeTo: shared.checkoutTime, twoGisLinkText: '', twoGisLinkUrl: '',
    wifiName: '', wifiPassword: '', additionalComment: '',
  };
}

Sections['rental-database'] = {
  async render(main) {
    const shared = await Api.getSharedFields();
    let draft = freshRentalDraft(shared);
    let records = await Api.list('rental-records');
    let showAll = false;

    main.innerHTML = '';
    main.appendChild(el('h2', {}, 'База данных'));
    const card = el('div', { class: 'card' });
    main.appendChild(card);
    card.appendChild(el('h3', {}, 'Новая запись об аренде'));

    card.appendChild(field('Код аэропорта', selectEl(['', ...AIRPORT_CODES], draft.airportCode, (v) => { draft.airportCode = v; })));
    card.appendChild(field('Город', el('input', { type: 'text', value: draft.city, oninput: (e) => { draft.city = e.target.value; } })));

    card.appendChild(el('h4', {}, 'Периодичность'));
    card.appendChild(tilePicker([['DAILY', 'Ежедневно'], ['MONTHLY', 'Ежемесячно']], draft.periodicity, (v) => { draft.periodicity = v; }));

    const dateRow = el('div', { class: 'row' });
    const startDate = el('input', { type: 'date', value: draft.rentStartDate || '' });
    const endDate = el('input', { type: 'date', value: draft.rentEndDate || '' });
    const durationText = el('p', {}, '');
    const totalText = el('p', { style: 'font-weight:600' }, '');
    function recomputeDerived() {
      draft.rentDurationDays = daysBetween(draft.rentStartDate, draft.rentEndDate);
      draft.rentTotalAmount = draft.rentAmount * draft.rentDurationDays;
      durationText.textContent = `Срок аренды: ${draft.rentDurationDays} дн.`;
      totalText.textContent = `Итого сумма аренды: ${formatMoney(draft.rentTotalAmount)}`;
    }
    startDate.addEventListener('change', () => { draft.rentStartDate = startDate.value || null; recomputeDerived(); });
    endDate.addEventListener('change', () => { draft.rentEndDate = endDate.value || null; recomputeDerived(); });
    dateRow.appendChild(field('Дата начала аренды', startDate));
    dateRow.appendChild(field('Дата завершения аренды', endDate));
    card.appendChild(dateRow);
    card.appendChild(durationText);

    const rentAmountInput = moneyInput(draft.rentAmount, (v) => { draft.rentAmount = v; recomputeDerived(); });
    card.appendChild(field('Сумма аренды', rentAmountInput));
    card.appendChild(totalText);
    const paymentDate = el('input', { type: 'date', value: draft.rentPaymentDate || '' });
    paymentDate.addEventListener('change', () => { draft.rentPaymentDate = paymentDate.value || null; });
    card.appendChild(field('Дата оплаты аренды', paymentDate));

    card.appendChild(field('Сумма депозита', moneyInput(draft.depositAmount, (v) => { draft.depositAmount = v; })));
    card.appendChild(el('h4', {}, 'Статус возврата депозита'));
    card.appendChild(tilePicker([['NONE', '(не указано)'], ['RETURNED', 'Вернули'], ['WITH_LANDLORD', 'У арендатора']], draft.depositReturnStatus, (v) => { draft.depositReturnStatus = v; }));

    card.appendChild(field('Департамент', selectEl(DEPARTMENTS, draft.department, (v) => { draft.department = v; })));
    card.appendChild(field('ФИО', el('input', { type: 'text', value: draft.fullName, oninput: (e) => { draft.fullName = e.target.value; } })));

    card.appendChild(el('h4', {}, 'Цель поездки'));
    card.appendChild(tilePicker([['WAREHOUSE_OPENING', 'В рамках открытия складов'], ['OTHER', 'Другое']], draft.purposeType, (v) => { draft.purposeType = v; }));
    card.appendChild(field('Цель поездки (другое)', el('input', { type: 'text', value: draft.purposeOtherText, oninput: (e) => { draft.purposeOtherText = e.target.value; } })));

    card.appendChild(el('h4', {}, 'Тип квартиры'));
    const apartmentOtherField = field('Тип (другое)', el('input', { type: 'text', value: draft.apartmentTypeOtherText, oninput: (e) => { draft.apartmentTypeOtherText = e.target.value; } }));
    apartmentOtherField.style.display = draft.apartmentType === 'OTHER' ? '' : 'none';
    card.appendChild(tilePicker(APARTMENT_TYPES.map(([c, l]) => [c, l]), draft.apartmentType, (v) => {
      draft.apartmentType = v; draft.roomsCount = apartmentRoomsCount(v);
      apartmentOtherField.style.display = v === 'OTHER' ? '' : 'none';
      roomsInput.value = draft.roomsCount || '';
    }));
    card.appendChild(apartmentOtherField);
    const roomsInput = el('input', { type: 'number', value: draft.roomsCount || '', oninput: (e) => { draft.roomsCount = Number(e.target.value) || 0; } });
    card.appendChild(field('Кол-во комнат', roomsInput));
    card.appendChild(field('Сколько проживает гостей', el('input', { type: 'number', value: draft.guestsCount || '', oninput: (e) => { draft.guestsCount = Number(e.target.value) || 0; } })));
    card.appendChild(field('Экономия при 2-местном размещении', moneyInput(draft.savingsTwoPerson, (v) => { draft.savingsTwoPerson = v; })));
    card.appendChild(field('Экономия при 1-местном размещении', moneyInput(draft.savingsOnePerson, (v) => { draft.savingsOnePerson = v; })));

    card.appendChild(field('Адрес', el('input', { type: 'text', value: draft.address, oninput: (e) => { draft.address = e.target.value; } })));
    card.appendChild(field('Квартплата', moneyInput(draft.utilitiesAmount, (v) => { draft.utilitiesAmount = v; })));

    const landlordCard = el('div', { class: 'card' });
    landlordCard.appendChild(el('h4', {}, 'Контакты арендодателя'));
    landlordCard.appendChild(field('ФИО арендодателя', el('input', { type: 'text', value: draft.landlordFullName, oninput: (e) => { draft.landlordFullName = e.target.value; } })));
    landlordCard.appendChild(field('Телефон', el('input', { type: 'text', value: draft.landlordPhone, oninput: (e) => { draft.landlordPhone = e.target.value; } })));
    landlordCard.appendChild(tilePicker([['INVOICE', 'Счёт'], ['TRANSFER', 'Перевод']], draft.landlordPaymentMethod, (v) => { draft.landlordPaymentMethod = v; }));
    landlordCard.appendChild(el('p', { class: 'small-muted', style: 'margin-top:8px' }, 'Рейтинг арендодателя'));
    landlordCard.appendChild(tilePicker(Array.from({ length: 10 }, (_, i) => [String(i + 1), String(i + 1)]), String(draft.landlordRating), (v) => { draft.landlordRating = Number(v); }));
    card.appendChild(landlordCard);

    const receiptCard = el('div', { class: 'card' });
    receiptCard.appendChild(el('h4', {}, 'Чек'));
    const receiptStatus = el('p', {}, draft.receiptFileName ? `Файл: ${draft.receiptFileName}` : '');
    receiptCard.appendChild(receiptStatus);
    const fileInput = el('input', { type: 'file' });
    fileInput.addEventListener('change', async () => {
      const file = fileInput.files[0];
      if (!file) return;
      const description = prompt('Что это за чек?', '') || '';
      const ext = file.name.includes('.') ? file.name.split('.').pop() : '';
      const dateStr = draft.rentStartDate || 'без_даты';
      const sanitize = (s) => (s || '').replace(/[\\/:*?"<>|]/g, '_');
      const fileName = `${dateStr}_${sanitize(draft.city || 'город')}_${sanitize(draft.fullName || 'ФИО')}_${sanitize(description)}${ext ? '.' + ext : ''}`;
      const uploadRes = await fetch('/api/upload', {
        method: 'POST', headers: { 'X-Original-Filename': encodeURIComponent(fileName) }, body: file,
      }).then((r) => r.json());
      draft.receiptFileName = fileName; draft.receiptUri = uploadRes.url; draft.receiptDescription = description;
      receiptStatus.textContent = `Файл: ${draft.receiptFileName}`;
    });
    receiptCard.appendChild(fileInput);
    card.appendChild(receiptCard);

    const statusMsg = el('p', { class: 'small-muted' }, '');
    const saveBtn = el('button', { style: 'margin-top:12px' }, 'Сохранить запись');
    saveBtn.addEventListener('click', async () => {
      recomputeDerived();
      const created = await Api.create('rental-records', draft);
      records.unshift(created);
      draft = freshRentalDraft(await Api.getSharedFields());
      statusMsg.textContent = 'Запись сохранена';
      renderRecordsList();
    });
    card.appendChild(saveBtn);
    card.appendChild(statusMsg);

    main.appendChild(el('hr'));
    main.appendChild(el('h3', {}, 'Записи'));
    const listHost = el('div');
    main.appendChild(listHost);
    const showAllBtn = el('button', { class: 'secondary' }, '');
    main.appendChild(showAllBtn);

    function renderRecordsList() {
      listHost.innerHTML = '';
      const visible = showAll ? records : records.slice(0, 5);
      for (const r of visible) {
        const rc = el('div', { class: 'card' });
        const header = el('div', { style: 'display:flex;justify-content:space-between' });
        header.appendChild(el('strong', {}, `${r.city} · ${r.fullName || '—'}`));
        const delBtn = el('button', { class: 'secondary' }, '✕');
        delBtn.addEventListener('click', async () => { await Api.remove('rental-records', r.id); records = records.filter((x) => x.id !== r.id); renderRecordsList(); });
        header.appendChild(delBtn);
        rc.appendChild(header);
        rc.appendChild(el('div', { class: 'small-muted' }, `${formatDate(r.rentStartDate)} – ${formatDate(r.rentEndDate)} (${r.rentDurationDays} дн.)`));
        rc.appendChild(el('div', {}, r.address));
        rc.appendChild(el('div', { class: 'small-muted' }, `${apartmentTypeLabel(r.apartmentType)} · ${formatMoney(r.rentAmount)}/сутки · Итого ${formatMoney(r.rentTotalAmount)}`));
        listHost.appendChild(rc);
      }
      showAllBtn.style.display = records.length > 5 ? '' : 'none';
      showAllBtn.textContent = showAll ? 'Показать только последние 5' : `Показать все записи (${records.length})`;
    }
    showAllBtn.addEventListener('click', () => { showAll = !showAll; renderRecordsList(); });
    renderRecordsList();

    main.appendChild(el('h3', { style: 'margin-top:24px' }, 'Импорт / экспорт'));
    const importInput = el('input', { type: 'file', accept: '.xlsx,.xls,.csv' });
    importInput.addEventListener('change', async () => {
      const file = importInput.files[0];
      if (!file) return;
      const { headers, rows } = await readXlsxOrCsv(file);
      const parsed = rows.map((row) => rowToRentalRecord(headers, row));
      for (const rec of parsed) records.unshift(await Api.create('rental-records', rec));
      statusMsg.textContent = `Импортировано записей: ${parsed.length}`;
      renderRecordsList();
    });
    main.appendChild(field('Импорт из Excel/CSV', importInput));

    const exportBtn = el('button', {}, 'Скачать Excel');
    exportBtn.addEventListener('click', () => {
      downloadXlsx('База данных аренды.xlsx', [{ name: 'База данных аренды', headers: RENTAL_HEADERS, rows: records.map(rentalRecordToRow) }]);
    });
    main.appendChild(exportBtn);

    main.appendChild(el('div', { style: 'height:12px' }));
    const backupBtn = el('button', { class: 'secondary' }, 'Резервное копирование');
    backupBtn.addEventListener('click', async () => {
      const [trips, landlordsAll, residentsAll, citiesAll, financeAll] = await Promise.all([
        Api.list('trip-entries'), Api.list('landlords'), Api.list('residents'), Api.list('cities'), Api.list('finance-registry'),
      ]);
      downloadXlsx('Резервная копия.xlsx', [
        { name: 'Входная информация', headers: ['ФИО проживающих', 'Должность', 'Департамент', 'Вид жилья', 'Вне регламента', 'Город назначения', 'Дата заселения', 'Дата выселения', 'Сумма суточных', 'Причина срочности', 'Обоснование командировки', 'Цель поездки', 'Цель поездки (другое)'],
          rows: trips.map((t) => [t.guestNames.filter(Boolean).join('; '), t.position, t.department, t.housingType === 'APARTMENT' ? 'Квартира' : 'Отель', t.offRegulation ? 'Да' : 'Нет', t.city, formatDate(t.checkinDate), formatDate(t.checkoutDate), t.perDiem, t.urgencyReason, t.justification, t.purposeType === 'OTHER' ? 'Другое' : 'В рамках открытия складов', t.purposeOtherText]) },
        { name: 'База данных аренды', headers: RENTAL_HEADERS, rows: records.map(rentalRecordToRow) },
        { name: 'Арендодатели', headers: ['ФИО', 'Контактный номер', 'Собственник/риелтор', 'Город', 'Способ оплаты', 'Рейтинг', 'Комментарий'],
          rows: landlordsAll.map((l) => [l.fullName, l.phone, l.ownerType === 'AGENT' ? 'Риелтор' : 'Собственник', l.city, l.paymentMethod === 'INVOICE' ? 'Счёт' : 'Перевод', l.rating, l.comment]) },
        { name: 'Проживающие сотрудники', headers: ['ФИО сотрудника', 'Комментарий'], rows: residentsAll.map((r) => [r.fullName, r.comment]) },
        { name: 'Города', headers: ['Город'], rows: citiesAll.map((c) => [c.name]) },
        { name: 'Реестр согласований', headers: ['Дата сохранения', 'Город', 'ФИО', 'Тип квартиры', 'Гостей', 'Заезд', 'Выезд', 'Суток', 'Ставка', 'Депозит', 'Итого', 'Комментарий'],
          rows: financeAll.map((f) => [formatDate(new Date(f.savedAt).toISOString().slice(0, 10)), f.city, f.fullName, apartmentTypeLabel(f.apartmentType), f.guestsCount, formatDate(f.checkinDate), formatDate(f.checkoutDate), f.nights, f.dailyRate, f.deposit, f.total, f.comment]) },
      ]);
    });
    main.appendChild(backupBtn);

    main.appendChild(el('div', { style: 'height:24px' }));
    const deleteAllBtn = el('button', { class: 'danger' }, '🗑️ Удалить все данные');
    deleteAllBtn.addEventListener('click', () => {
      confirmDeleteAllDialog(async () => {
        await Api.deleteAllData();
        location.reload();
      });
    });
    main.appendChild(deleteAllBtn);
  },
};
