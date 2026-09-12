Sections['residents'] = {
  async render(main) {
    let residents = await Api.list('residents');
    const form = { fullName: '', comment: '' };

    main.innerHTML = '';
    main.appendChild(el('h2', {}, 'Проживающие сотрудники'));
    const card = el('div', { class: 'card' });
    main.appendChild(card);
    card.appendChild(el('h3', {}, 'Новый сотрудник'));
    card.appendChild(field('ФИО сотрудника', el('input', { type: 'text', oninput: (e) => { form.fullName = e.target.value; } })));
    card.appendChild(field('Комментарий', el('input', { type: 'text', placeholder: 'Например: Проживает в Алматы до 20.08.2026', oninput: (e) => { form.comment = e.target.value; } })));
    const addBtn = el('button', { style: 'margin-top:12px' }, 'Добавить');
    card.appendChild(addBtn);

    const listHost = el('div');
    main.appendChild(listHost);

    function renderList() {
      listHost.innerHTML = '';
      for (const r of residents) {
        const c = el('div', { class: 'card', style: 'display:flex;justify-content:space-between' });
        const info = el('div');
        info.appendChild(el('div', {}, r.fullName));
        if (r.comment) info.appendChild(el('div', { class: 'small-muted' }, r.comment));
        c.appendChild(info);
        const delBtn = el('button', { class: 'secondary' }, '✕');
        delBtn.addEventListener('click', async () => { await Api.remove('residents', r.id); residents = residents.filter((x) => x.id !== r.id); renderList(); });
        c.appendChild(delBtn);
        listHost.appendChild(c);
      }
    }
    renderList();

    addBtn.addEventListener('click', async () => {
      if (!form.fullName.trim()) return;
      const created = await Api.create('residents', { ...form });
      residents.unshift(created);
      renderList();
    });

    const exportBtn = el('button', { class: 'secondary', style: 'margin-top:12px' }, 'Скачать Excel');
    exportBtn.addEventListener('click', () => {
      downloadXlsx('Проживающие сотрудники.xlsx', [{ name: 'Проживающие сотрудники', headers: ['ФИО сотрудника', 'Комментарий'], rows: residents.map((r) => [r.fullName, r.comment]) }]);
    });
    main.appendChild(exportBtn);
  },
};
