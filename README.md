# TalentFlow CRM

Рекрутерская CRM-система с полноценным бэкендом.

## Запуск

**Требования:** Node.js 22+

```bash
# Перейдите в папку проекта
cd talentflow-crm

# Запустите сервер
node server.js

# Откройте в браузере
# http://localhost:3000
```

Данные автоматически сохраняются в файл `crm.db` (SQLite).

## Структура

```
talentflow-crm/
├── server.js        ← бэкенд (Node.js, без зависимостей)
├── crm.db           ← база данных SQLite (создаётся автоматически)
├── README.md
└── public/
    └── index.html   ← фронтенд
```

## REST API

| Метод  | URL                                 | Описание                   |
|--------|-------------------------------------|----------------------------|
| GET    | /api/candidates                     | Список кандидатов          |
| GET    | /api/candidates?q=текст&stage=Этап  | Поиск и фильтрация         |
| POST   | /api/candidates                     | Добавить кандидата         |
| PATCH  | /api/candidates/:id                 | Обновить кандидата         |
| DELETE | /api/candidates/:id                 | Удалить кандидата          |
| GET    | /api/candidates/:id/messages        | Переписка кандидата        |
| POST   | /api/candidates/:id/messages        | Отправить сообщение        |
| GET    | /api/candidates/:id/history         | История действий           |
| GET    | /api/candidates/:id/resume          | Резюме (опыт, образование) |
| GET    | /api/analytics                      | Аналитика и воронка        |
| GET    | /api/integrations                   | Список интеграций          |
| POST   | /api/integrations/:id/sync          | Запустить синхронизацию    |

## Смена порта

```bash
PORT=8080 node server.js
```
