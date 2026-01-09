-- changeset init-orders-:001

CREATE TABLE IF NOT EXISTS orders
(
    id                  UUID PRIMARY KEY DEFAULT uuidv7(),
    customer_id         BIGSERIAL      NOT NULL,
    address             TEXT,
    payment_status      INTEGER        NOT NULL,
    client_estimate     NUMERIC(19, 2) NOT NULL,
    authorized_amount   NUMERIC(19, 2),
    final_amount        NUMERIC(19, 2),
    capture_amount      NUMERIC(19, 2),
    failure_reason      TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_orders_payment_status ON orders (payment_status);

CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders (created_at);

CREATE INDEX IF NOT EXISTS idx_orders_updated_at ON orders (updated_at);