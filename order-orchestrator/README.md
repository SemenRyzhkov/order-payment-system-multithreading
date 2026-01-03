# order-orchestrator

Сервис заказов и каркас оркестрации платежей. HTTP-слой готов, бизнес-логика оркестратора заполняется студентами.

## API
- `POST /order` — создать заказ. Тело: `OrderCreateRequestDto` (пока адрес; остальные поля добавляются студентами). Возвращает `OrderDto` c `id`.
- `GET /order/{id}` — получить заказ по id (возвращает адрес/id; статусы/суммы добавляются студентами).

## Конфигурация (env)
- `SPRING_DATASOURCE_URL` — JDBC для Postgres.
- `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD`.
- `PAYMENT_STUB_URL` — URL платежного/warehouse стаба (например, `http://payment-stub:8081`).
- Liquibase changelog: `classpath:/db/changelog/changelog-master.yaml`.

## Сборка и запуск
```bash
# сборка jar
./gradlew :order-orchestrator:bootJar

# локальный запуск (нужен доступ к БД и стабу чтобы успешно запустился)
./gradlew :order-orchestrator:bootRun
```

## Docker
- Dockerfile лежит в корне модуля.
- Для общего запуска см. `infra/docker-compose.dev.yaml` (нужна сборка образов заранее).

## Что делает студент
- Добавляет недостающие поля/таблицы (orders/payments/payment_tasks).
- Реализует оркестрацию (AUTH → REPRICE → CAPTURE/FAIL) в сервисе задач.
- Настраивает клиенты к платежке/складу и транзакционный outbox/poller.
