# Order Payment System

Шаблон для ДЗ по асинхронной оплате заказов (AsyncRunner 2049). Содержит общий модуль с DTO, оркестратор заказов и стабы внешних сервисов.

## Архитектура
- `common-libs` — общие модели/DTO для платежного шлюза и ценового сервиса.
- `order-orchestrator` — API для заказов + каркас оркестрации платежа (бизнес-логика пишется студентами).
- `payment-stub` — HTTP-стабы платежного шлюза и пересчёта цен с настраиваемым поведением.
- `infra/docker-compose.dev.yaml` — общий compose для Postgres + стобы + оркестратор (образы нужно собрать заранее).
- `order-orchestrator/docker-compose.dev.yaml` — локальный compose для оркестратора (Postgres + stubs) при запуске через Spring Docker Compose plugin.

Все модули собраны на Java 21 / Spring Boot 3.5.7. Gradle настроен на toolchain 21, так что системная JDK не критична.

## Быстрый старт
1. Убедись, что есть Docker и JDK 21.
2. Собрать артефакты:
   ```bash
   ./gradlew :payment-stub:bootJar :order-orchestrator:bootJar
   ```
3. Собрать образы:

   - Собрать образ stub'а:
      ```bash
      bash ./infra/build-stub-image.dev.sh
      ```

   - Собрать образ оркестратора:
      ```bash
      bash ./infra/build-orchestrator-image.dev.sh
      ```
4. Запустить весь стек:
   ```bash
   docker compose -f infra/docker-compose.dev.yaml up --build
   ```
   По умолчанию: оркестратор на `http://localhost:8080`, стаб на `http://localhost:8081`, Postgres на `localhost:5432`.

## Запуск сервисов отдельно
- Оркестратор локально:
  ```bash
  ./gradlew :order-orchestrator:bootRun
  ```
- Стаб локально:
  ```bash
  ./gradlew :payment-stub:bootRun
  ```
  Конфигурация задаётся env-переменными `STUB_PAYMENT_*` и `STUB_WAREHOUSE_*` (см. README модуля).

## Swagger/UI
- Оркестратор (если включён SpringDoc): `http://localhost:8080/swagger-ui/index.html`
- Payment Stub: `http://localhost:8081/swagger-ui/index.html`

## Compose варианты
- Общий стек: `infra/docker-compose.dev.yaml` — весь набор (Postgres + стобы + оркестратор).
- Локальный для оркестратора: `order-orchestrator/docker-compose.dev.yaml` — можно стартовать через Spring Docker Compose plugin при `bootRun`.

## Полезные команды для общего Docker Compose
- Старт общего стека (с пересборкой):  
  ```bash
  docker compose -f infra/docker-compose.dev.yaml up --build
  ```
- Остановка с удалением контейнеров/сетей (тома остаются):  
  ```bash
  docker compose -f infra/docker-compose.dev.yaml down
  ```
- Полное удаление с томами:  
  ```bash
  docker compose -f infra/docker-compose.dev.yaml down -v
  ```
- Перезапуск с подтягиванием новой конфигурации/образов:  
  ```bash
  docker compose -f infra/docker-compose.dev.yaml up -d --build --force-recreate
  ```
- Просмотр логов:  
  ```bash
  docker compose -f infra/docker-compose.dev.yaml logs -f
  ```

## Документация по модулям
- `common-libs/README.md` — что внутри и как использовать.
- `order-orchestrator/README.md` — API заказов, конфигурация, запуск.
- `payment-stub/README.md` — эндпоинты стаба, конфигурация, запуск, Swagger.
