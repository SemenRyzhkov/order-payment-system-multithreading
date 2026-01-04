-- changeset init-payment-tasks-:002

CREATE TABLE IF NOT EXISTS payment_tasks
(
    id                  UUID PRIMARY KEY DEFAULT uuidv7(),
    order_id            UUID        NOT NULL,
    status              INTEGER     NOT NULL,
    step                INTEGER,
    attempts            INTEGER NOT NULL DEFAULT 0,
    next_attempt_at     TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    locked_until        TIMESTAMPTZ
    );

-- Индекс для эффективного выбора задач, готовых к обработке
CREATE INDEX IF NOT EXISTS idx_payment_tasks_status_next_attempt
    ON payment_tasks (status, next_attempt_at);

-- Индекс для поиска задач по заказу
CREATE INDEX IF NOT EXISTS idx_payment_tasks_order_id
    ON payment_tasks (order_id);

-- Индекс для обработки конкурентных блокировок
CREATE INDEX IF NOT EXISTS idx_payment_tasks_locked_until
    ON payment_tasks (locked_until);

-- Уникальный индекс, чтобы избежать дублирования задач на один шаг для одного заказа
CREATE UNIQUE INDEX IF NOT EXISTS uq_payment_tasks_order_id_step
    ON payment_tasks (order_id, step);