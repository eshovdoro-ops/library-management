# Система управления библиотекой

REST API на **Java 21 + Spring Boot 4.1 + PostgreSQL**. Книги, читатели, выдача и возврат, поиск, задолженности.
Авторизация — JWT, роли `ADMIN` и `LIBRARIAN`.

## Возможности
| Требование | Реализация |
|---|---|
| Авторизация | `POST /api/auth/login` → JWT; роли ADMIN / LIBRARIAN |
| Добавление / просмотр / редактирование / удаление | CRUD для книг и читателей, выдача и возврат |
| Поиск, фильтрация, сортировка | `GET /api/books?q=&onlyAvailable=&sort=title,asc&page=0&size=20`, `GET /api/loans?status=OVERDUE` |
| Работа с БД | PostgreSQL, Spring Data JPA, миграции Flyway |
| Обработка ошибок | единый JSON-ответ (400/401/403/404/409/500), без stack trace |
| Аналитический отчёт | `GET /api/reports/debts` — задолженности читателей и штрафы |
| Тестирование | JUnit/Mockito + 20 сценариев в `docs/TEST_SCENARIOS.md` + коллекция Postman |

## Запуск
Требуется: JDK 21, Maven 3.9+, PostgreSQL 16 (или Docker).

1. `cp .env.example .env` и заполните значения (пароли придумайте сами, `JWT_SECRET` — не короче 32 символов: `openssl rand -base64 48`).
2. База данных: `docker compose --env-file .env up -d` (либо создайте БД `library` в своей PostgreSQL).
3. Загрузите переменные окружения и запустите:
   - Linux/macOS: `set -a; source .env; set +a; mvn spring-boot:run`
   - Windows PowerShell: задайте `$env:DB_USER="..."` и т. д., затем `mvn spring-boot:run`
   - IntelliJ IDEA: Run → Edit Configurations → Environment variables.
4. Тесты: `mvn test` (не требуют БД). В GitHub Actions выполняются `mvn verify` и проверка уязвимостей зависимостей; Dependabot проверяет Maven-зависимости и GitHub Actions.

Без `DB_USER`, `DB_PASSWORD` и `JWT_SECRET` приложение намеренно не запустится.

## Публикация в GitHub

1. Создайте пустой репозиторий в своём GitHub-профиле. Для первой публикации безопаснее выбрать **Private**; публичный доступ можно включить после проверки содержимого.
2. В PowerShell из папки проекта проверьте, что локальные секреты не попадут в Git, затем добавьте изменения и создайте коммит:

   ```powershell
   git status --short
   git check-ignore .env
   git add .
   git commit -m "Prepare library management project"
   ```

   Если `git check-ignore .env` не выводит путь, не создавайте и не добавляйте `.env`; проверьте `.gitignore`. Не публикуйте пароли, JWT-ключи, дампы базы данных или реальные учётные данные.
3. Подставьте URL созданного репозитория вместо `<URL-репозитория>`:

   ```powershell
   git remote add origin <URL-репозитория>
   git push -u origin main
   ```

   Если remote `origin` уже настроен, проверьте его командой `git remote -v` и при необходимости используйте URL нового репозитория командой `git remote set-url origin <URL-репозитория>`.
4. В GitHub откройте вкладку **Actions** и проверьте workflow **Java CI**. Он собирает приложение, запускает тесты и Dependency-Check. Для надёжной работы сканера добавьте секрет `NVD_API_KEY` в **Settings → Secrets and variables → Actions**; без него NVD может ограничивать частоту запросов.

## Postman
Импортируйте `postman/library-api.postman_collection.json`, в переменных коллекции задайте `adminUser` / `adminPassword`
(те же, что `ADMIN_USERNAME` / `ADMIN_PASSWORD`), запустите «Login» — токен сохранится автоматически.

## Бизнес-правила
Срок выдачи 14 дней; не более 5 книг на руках; читателю с просрочкой книги не выдаются; штраф 0.50 за день (`application.yml`).

## Безопасность
См. `docs/SECURITY.md`.
