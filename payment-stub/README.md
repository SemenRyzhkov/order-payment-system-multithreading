# payment-stub

HTTP-стады платежного шлюза и сервиса пересчёта цен с настраиваемыми задержками/ошибками.

## Эндпоинты
- `POST /payment/authorize` — авторизация холда.
  - Request: `{ "customerId": 1, "amount": 5000 }`
  - Response: `{ "authorizationId": "...", "authorizedAmount": 5000, "status": "AUTHORIZED|DECLINED", "message": "..." }`
- `POST /payment/capture` — списание.
  - Request: `{ "captureAmount": 4800, "customerId": 1 }`
  - Response: `{ "captureId": "...", "capturedAmount": 4800, "status": "CAPTURED|FAILED", "message": "..." }`
- `POST /warehouse/calculate-price` — пересчёт цены заказа.
  - Request: `{ "orderId": "UUID" }`
  - Response: `{ "orderId": "UUID", "finalAmount": 1234, "reason": "Price calculated by stub" }`

Swagger UI: `http://localhost:8081/swagger-ui/index.html` (можно отключить флагами SpringDoc).

## Конфигурация (env)
Все параметры могут задаваться через переменные окружения, дефолты прописаны в `application.yml`.

### Платёжный стаб (`stub.payment.*`)
- `STUB_PAYMENT_DECLINE_ENABLED` (true/false)
- `STUB_PAYMENT_DECLINE_PROBABILITY` (0..1)
- `STUB_PAYMENT_AUTHORIZE_LATENCY_MIN_MILLIS` / `STUB_PAYMENT_AUTHORIZE_LATENCY_MAX_MILLIS`
- `STUB_PAYMENT_CAPTURE_LATENCY_MIN_MILLIS` / `STUB_PAYMENT_CAPTURE_LATENCY_MAX_MILLIS`
- `STUB_PAYMENT_EXCEPTION_ENABLED` (true/false)
- `STUB_PAYMENT_EXCEPTION_PROBABILITY` (0..1)

### Стаб пересчёта (`stub.warehouse.*`)
- `STUB_WAREHOUSE_LATENCY_MIN_MILLIS` / `STUB_WAREHOUSE_LATENCY_MAX_MILLIS`
- `STUB_WAREHOUSE_EXCEPTION_ENABLED` (true/false)
- `STUB_WAREHOUSE_EXCEPTION_PROBABILITY` (0..1)
- `STUB_WAREHOUSE_FINAL_AMOUNT_MIN` / `STUB_WAREHOUSE_FINAL_AMOUNT_MAX`

## Сборка и запуск
```bash
# сборка jar
./gradlew :payment-stub:bootJar

# локальный запуск
./gradlew :payment-stub:bootRun
```

## Docker
- Dockerfile в корне модуля.
- Быстрый билд образа: `./infra/build-stub-image.dev.sh`.
- В составе стека поднимается через `infra/docker-compose.dev.yaml` на порту 8081.
