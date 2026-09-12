Sections['cities'] = {
  async render(main) {
    let cities = await Api.list('cities');

    main.innerHTML = '';
    main.appendChild(el('h2', {}, `Города (${cities.length})`));

    const row = el('div', { class: 'row', style: 'align-items:end;max-width:420px' });
    const input = el('input', { type: 'text', placeholder: 'Новый город' });
    row.appendChild(field('Новый город', input));
    const addBtn = el('button', {}, 'Добавить');
    row.appendChild(addBtn);
    main.appendChild(row);

    const listHost = el('div', { style: 'margin-top:12px' });
    main.appendChild(listHost);

    function renderList() {
      listHost.innerHTML = '';
      for (const c of cities.sort((a, b) => a.name.localeCompare(b.name, 'ru'))) {
        const card = el('div', { class: 'card', style: 'display:flex;justify-content:space-between;align-items:center;padding:10px 16px' });
        card.appendChild(el('span', {}, c.name));
        const delBtn = el('button', { class: 'secondary' }, '✕');
        delBtn.addEventListener('click', async () => { await Api.remove('cities', c.id); cities = cities.filter((x) => x.id !== c.id); renderList(); });
        card.appendChild(delBtn);
        listHost.appendChild(card);
      }
    }
    renderList();

    addBtn.addEventListener('click', async () => {
      const name = input.value.trim();
      if (!name) return;
      const created = await Api.create('cities', { name });
      cities.push(created);
      input.value = '';
      renderList();
    });

    const resetBtn = el('button', { class: 'secondary', style: 'margin-top:16px' }, 'Восстановить список по умолчанию');
    resetBtn.addEventListener('click', async () => {
      cities = await Api.resetCities();
      renderList();
    });
    main.appendChild(resetBtn);
  },
};
