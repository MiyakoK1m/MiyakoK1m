Sections['landlords'] = {
  async render(main) {
    let landlords = await Api.list('landlords');
    const cities = await Api.list('cities');
    const form = { fullName: '', phone: '', ownerType: 'OWNER', city: '', paymentMethod: 'TRANSFER', rating: 7, comment: '' };

    main.innerHTML = '';
    main.appendChild(el('h2', {}, 'Арендодатели'));
    const card = el('div', { class: 'card' });
    main.appendChild(card);
    card.appendChild(el('h3', {}, 'Новый арендодатель'));
    card.appendChild(field('ФИО арендодателя', el('input', { type: 'text', oninput: (e) => { form.fullName = e.target.value; } })));
    card.appendChild(field('Контактный номер', el('input', { type: 'text', oninput: (e) => { form.phone = e.target.value; } })));
    card.appendChild(tilePicker([['OWNER', 'Собственник'], ['AGENT', 'Риелтор']], form.ownerType, (v) => { form.ownerType = v; }));
    const cityF = field('Город', datalistInput('landlord-city-suggest', cities.map((c) => c.name), ''));
    cityF.querySelector('input').addEventListener('input', (e) => { form.city = e.target.value; });
    card.appendChild(cityF);
    card.appendChild(tilePicker([['INVOICE', 'Счёт'], ['TRANSFER', 'Перевод']], form.paymentMethod, (v) => { form.paymentMethod = v; }));
    card.appendChild(el('p', { class: 'small-muted' }, 'Рейтинг'));
    card.appendChild(tilePicker(Array.from({ length: 10 }, (_, i) => [String(i + 1), String(i + 1)]), String(form.rating), (v) => { form.rating = Number(v); }));
    card.appendChild(field('Комментарий', el('input', { type: 'text', oninput: (e) => { form.comment = e.target.value; } })));

    const addBtn = el('button', { style: 'margin-top:12px' }, 'Добавить');
    card.appendChild(addBtn);

    const listHost = el('div');
    main.appendChild(listHost);

    function renderList() {
      listHost.innerHTML = '';
      for (const l of landlords) {
        const c = el('div', { class: 'card', style: 'display:flex;justify-content:space-between' });
        const info = el('div');
        info.appendChild(el('div', {}, l.fullName));
        info.appendChild(el('div', { class: 'small-muted' }, `${l.phone} · ${l.city} · ★${l.rating}`));
        if (l.comment) info.appendChild(el('div', { class: 'small-muted' }, l.comment));
        c.appendChild(info);
        const delBtn = el('button', { class: 'secondary' }, '✕');
        delBtn.addEventListener('click', async () => { await Api.remove('landlords', l.id); landlords = landlords.filter((x) => x.id !== l.id); renderList(); });
        c.appendChild(delBtn);
        listHost.appendChild(c);
      }
    }
    renderList();

    addBtn.addEventListener('click', async () => {
      if (!form.fullName.trim()) return;
      const created = await Api.create('landlords', { ...form });
      landlords.unshift(created);
      renderList();
    });

    const exportBtn = el('button', { class: 'secondary', style: 'margin-top:12px' }, 'Скачать Excel');
    exportBtn.addEventListener('click', () => {
      downloadXlsx('Арендодатели.xlsx', [{
        name: 'Арендодатели', headers: ['ФИО', 'Контактный номер', 'Собственник/риелтор', 'Город', 'Способ оплаты', 'Рейтинг', 'Комментарий'],
        rows: landlords.map((l) => [l.fullName, l.phone, l.ownerType === 'AGENT' ? 'Риелтор' : 'Собственник', l.city, l.paymentMethod === 'INVOICE' ? 'Счёт' : 'Перевод', l.rating, l.comment]),
      }]);
    });
    main.appendChild(exportBtn);
  },
};
