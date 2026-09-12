const RENTAL_INFO_COLUMNS = [
  ['Код аэропорта', 'airportCode', 'text'],
  ['Город', 'city', 'text'],
  ['Периодичность', 'periodicity', 'enum', { DAILY: 'Ежедневно', MONTHLY: 'Ежемесячно' }],
  ['Дата начала аренды', 'rentStartDate', 'date'],
  ['Дата завершения аренды', 'rentEndDate', 'date'],
  ['Срок (дней)', 'rentDurationDays', 'number'],
  ['Сумма аренды', 'rentAmount', 'number'],
  ['Итого сумма аренды', 'rentTotalAmount', 'number'],
  ['Дата оплаты аренды', 'rentPaymentDate', 'date'],
  ['Депозит', 'depositAmount', 'number'],
  ['Возврат депозита', 'depositReturnStatus', 'enum', { NONE: '', RETURNED: 'Вернули', WITH_LANDLORD: 'У арендатора' }],
  ['Департамент', 'department', 'text'],
  ['ФИО', 'fullName', 'text'],
  ['Цель поездки', 'purposeType', 'enum', { WAREHOUSE_OPENING: 'В рамках открытия складов', OTHER: 'Другое' }],
  ['Цель поездки (другое)', 'purposeOtherText', 'text'],
  ['Кол-во комнат', 'roomsCount', 'number'],
  ['Гостей', 'guestsCount', 'number'],
  ['Экономия 2-местное', 'savingsTwoPerson', 'number'],
  ['Экономия 1-местное', 'savingsOnePerson', 'number'],
  ['Адрес', 'address', 'text'],
  ['Тип квартиры', 'apartmentType', 'apartmentType'],
  ['Тип квартиры (другое)', 'apartmentTypeOtherText', 'text'],
  ['Квартплата', 'utilitiesAmount', 'number'],
  ['Арендодатель ФИО', 'landlordFullName', 'text'],
  ['Арендодатель телефон', 'landlordPhone', 'text'],
  ['Способ оплаты', 'landlordPaymentMethod', 'enum', { INVOICE: 'Счёт', TRANSFER: 'Перевод' }],
  ['Рейтинг арендодателя', 'landlordRating', 'number'],
  ['Файл чека', 'receiptFileName', 'text'],
  ['Описание чека', 'receiptDescription', 'text'],
  ['ЖК', 'complexName', 'text'],
  ['Подъезд', 'entrance', 'text'],
  ['Примечание к подъезду', 'entranceNote', 'text'],
  ['Этаж', 'floor', 'text'],
  ['Номер квартиры', 'apartmentNumber', 'text'],
  ['Код домофона', 'intercomCode', 'text'],
  ['Время заселения', 'checkinTimeFrom', 'text'],
  ['Время выселения', 'checkoutTimeTo', 'text'],
  ['2ГИС текст', 'twoGisLinkText', 'text'],
  ['2ГИС ссылка', 'twoGisLinkUrl', 'text'],
  ['Wi-Fi имя', 'wifiName', 'text'],
  ['Wi-Fi пароль', 'wifiPassword', 'text'],
  ['Комментарий', 'additionalComment', 'text'],
];

function cellDisplayValue(record, field, kind, enumMap) {
  const raw = record[field];
  if (kind === 'date') return formatDate(raw);
  if (kind === 'enum') return enumMap[raw] ?? raw ?? '';
  if (kind === 'apartmentType') return record.apartmentType === 'OTHER' ? (record.apartmentTypeOtherText || apartmentTypeLabel('OTHER')) : apartmentTypeLabel(raw);
  return raw ?? '';
}

function cellParsedValue(text, kind, enumMap) {
  if (kind === 'number') return Number(text) || 0;
  if (kind === 'date') return parseDateFlexible(text);
  if (kind === 'enum') {
    const entry = Object.entries(enumMap).find(([, label]) => label.toLowerCase() === text.trim().toLowerCase());
    return entry ? entry[0] : text;
  }
  return text;
}

Sections['rental-info-table'] = {
  async render(main) {
    let records = await Api.list('rental-records');
    let query = '';

    main.innerHTML = '';
    main.appendChild(el('h2', {}, 'Информация по аренде'));

    const searchInput = el('input', { type: 'text', placeholder: 'Поиск по ФИО' });
    main.appendChild(field('Поиск по ФИО', searchInput));

    const tableHost = el('div', { class: 'table-scroll card' });
    main.appendChild(tableHost);

    function renderTable() {
      tableHost.innerHTML = '';
      const filtered = query ? records.filter((r) => (r.fullName || '').toLowerCase().includes(query.toLowerCase())) : records;
      const table = el('table');
      table.appendChild(el('tr', {}, RENTAL_INFO_COLUMNS.map(([label]) => el('th', {}, label))));
      for (const record of filtered) {
        const tr = el('tr');
        for (const [, fieldName, kind, enumMap] of RENTAL_INFO_COLUMNS) {
          const td = el('td', { contenteditable: 'true' }, cellDisplayValue(record, fieldName, kind, enumMap));
          td.addEventListener('blur', async () => {
            const newValue = cellParsedValue(td.textContent, kind, enumMap);
            if (String(newValue) === String(record[fieldName])) return;
            record[fieldName] = newValue;
            await Api.update('rental-records', record.id, record);
            td.textContent = cellDisplayValue(record, fieldName, kind, enumMap);
          });
          tr.appendChild(td);
        }
        table.appendChild(tr);
      }
      tableHost.appendChild(table);
    }

    searchInput.addEventListener('input', debounce(() => { query = searchInput.value; renderTable(); }, 200));
    renderTable();
  },
};
