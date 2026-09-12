Sections['checkin-info'] = {
  async render(main) {
    const [shared, templates] = await Promise.all([Api.getSharedFields(), Api.getTemplates()]);
    const local = JSON.parse(sessionStorage.getItem('checkin-local') || 'null') || {
      entrance: '', entranceNote: '', floor: '', apartmentNumber: '', intercomCode: '',
      twoGisLinkText: '', twoGisLinkUrl: '', wifiName: '', wifiPassword: '', includeRulesBlock: true, additionalComment: '',
    };
    const saveLocal = () => sessionStorage.setItem('checkin-local', JSON.stringify(local));
    let template = templates.CHECKIN;

    const RULES_BLOCK = `Правила проживания:
• Курение в квартире запрещено
• Просьба поддерживать чистоту: не оставлять после себя беспорядок, не наносить ущерб имуществу, бережно относиться к постельному белью и полотенцам
• С 23:00 соблюдать тишину, уважая соседей
• При вызове полиции из-за нарушения тишины или иных правил ответственность несёт сотрудник
• При нарушении правил проживания возвратный депозит не возвращается — ответственность также несёт сотрудник`;

    main.innerHTML = '';
    main.appendChild(el('h2', {}, 'Информация по заселению'));
    const card = el('div', { class: 'card' });
    main.appendChild(card);

    const addressInput = el('input', { type: 'text', value: shared.complexName });
    card.appendChild(field('ЖК', addressInput));
    const address2 = el('input', { type: 'text', value: shared.address });
    card.appendChild(field('Адрес', address2));

    const row1 = el('div', { class: 'row' });
    const entranceInput = el('input', { type: 'text', value: local.entrance });
    const entranceNoteInput = el('input', { type: 'text', value: local.entranceNote });
    row1.appendChild(field('Подъезд', entranceInput));
    row1.appendChild(field('Примечание', entranceNoteInput));
    card.appendChild(row1);

    const row2 = el('div', { class: 'row' });
    const floorInput = el('input', { type: 'text', value: local.floor });
    const aptNumberInput = el('input', { type: 'text', value: local.apartmentNumber });
    row2.appendChild(field('Этаж', floorInput));
    row2.appendChild(field('Номер квартиры', aptNumberInput));
    card.appendChild(row2);

    const intercomInput = el('input', { type: 'text', value: local.intercomCode });
    card.appendChild(field('Код домофона', intercomInput));

    const row3 = el('div', { class: 'row' });
    const checkinTimeInput = el('input', { type: 'text', value: shared.checkinTime, placeholder: '14:00' });
    const checkoutTimeInput = el('input', { type: 'text', value: shared.checkoutTime, placeholder: '12:00' });
    row3.appendChild(field('Время заселения (с)', checkinTimeInput));
    row3.appendChild(field('Время выселения (до)', checkoutTimeInput));
    card.appendChild(row3);

    const row4 = el('div', { class: 'row' });
    const gisTextInput = el('input', { type: 'text', value: local.twoGisLinkText });
    const gisUrlInput = el('input', { type: 'text', value: local.twoGisLinkUrl });
    row4.appendChild(field('Ссылка 2ГИС — текст', gisTextInput));
    row4.appendChild(field('Ссылка 2ГИС — URL', gisUrlInput));
    card.appendChild(row4);

    const row5 = el('div', { class: 'row' });
    const wifiNameInput = el('input', { type: 'text', value: local.wifiName });
    const wifiPassInput = el('input', { type: 'text', value: local.wifiPassword });
    row5.appendChild(field('Имя Wi-Fi сети', wifiNameInput));
    row5.appendChild(field('Пароль Wi-Fi', wifiPassInput));
    card.appendChild(row5);

    const rulesLabel = el('label', {});
    const rulesCheckbox = el('input', { type: 'checkbox' });
    rulesCheckbox.checked = local.includeRulesBlock;
    rulesLabel.appendChild(rulesCheckbox);
    rulesLabel.appendChild(document.createTextNode(' Включить стандартный блок правил проживания'));
    card.appendChild(el('div', { class: 'field' }, rulesLabel));

    const commentInput = el('textarea', {}, local.additionalComment);
    card.appendChild(field('Дополнительный комментарий', commentInput));

    main.appendChild(el('h3', {}, 'Шаблон (редактируемый, токены вида {жк}, {адрес}, {дата_заезда}…)'));
    const templateArea = el('textarea', { style: 'width:100%;min-height:180px' }, template);
    main.appendChild(templateArea);

    main.appendChild(el('h3', {}, 'Готовый текст'));
    const output = el('textarea', { class: 'copy-box', readonly: 'readonly', style: 'width:100%;min-height:200px' });
    const copyBox = el('div', { class: 'copy-box' });
    copyBox.appendChild(output);
    const copyBtn = el('button', {}, 'Скопировать');
    copyBtn.addEventListener('click', () => { navigator.clipboard.writeText(output.value); });
    copyBox.appendChild(copyBtn);
    main.appendChild(copyBox);

    function recompute() {
      const tokens = {
        'жк': shared.complexName, 'адрес': shared.address,
        'подъезд': [local.entrance, local.entranceNote].filter(Boolean).join(' '),
        'этаж': local.floor, 'номер_квартиры': local.apartmentNumber, 'домофон': local.intercomCode,
        'дата_заезда': formatDate(shared.checkinDateIso), 'время_заезда': shared.checkinTime,
        'дата_выезда': formatDate(shared.checkoutDateIso), 'время_выезда': shared.checkoutTime,
        '2гис': (local.twoGisLinkText && local.twoGisLinkUrl) ? `[${local.twoGisLinkText}](${local.twoGisLinkUrl})` : '',
        'wifi_имя': local.wifiName, 'wifi_пароль': local.wifiPassword,
        'правила': local.includeRulesBlock ? RULES_BLOCK : '', 'комментарий': local.additionalComment,
      };
      output.value = renderTemplate(templateArea.value, tokens);
    }

    addressInput.addEventListener('change', async () => { shared.complexName = addressInput.value; await Api.updateSharedFields({ complexName: shared.complexName }); recompute(); });
    address2.addEventListener('change', async () => { shared.address = address2.value; await Api.updateSharedFields({ address: shared.address }); recompute(); });
    checkinTimeInput.addEventListener('change', async () => { shared.checkinTime = checkinTimeInput.value; await Api.updateSharedFields({ checkinTime: shared.checkinTime }); recompute(); });
    checkoutTimeInput.addEventListener('change', async () => { shared.checkoutTime = checkoutTimeInput.value; await Api.updateSharedFields({ checkoutTime: shared.checkoutTime }); recompute(); });

    [entranceInput, entranceNoteInput, floorInput, aptNumberInput, intercomInput, gisTextInput, gisUrlInput, wifiNameInput, wifiPassInput, commentInput].forEach((input) => {
      input.addEventListener('input', () => {
        local.entrance = entranceInput.value; local.entranceNote = entranceNoteInput.value;
        local.floor = floorInput.value; local.apartmentNumber = aptNumberInput.value; local.intercomCode = intercomInput.value;
        local.twoGisLinkText = gisTextInput.value; local.twoGisLinkUrl = gisUrlInput.value;
        local.wifiName = wifiNameInput.value; local.wifiPassword = wifiPassInput.value; local.additionalComment = commentInput.value;
        saveLocal();
        recompute();
      });
    });
    rulesCheckbox.addEventListener('change', () => { local.includeRulesBlock = rulesCheckbox.checked; saveLocal(); recompute(); });
    templateArea.addEventListener('input', () => { Api.updateTemplate('CHECKIN', templateArea.value); recompute(); });

    recompute();
  },
};
