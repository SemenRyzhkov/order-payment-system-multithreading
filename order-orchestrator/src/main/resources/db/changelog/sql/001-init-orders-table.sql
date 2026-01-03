-- changeset init-orders-:001

-- TODO остальные поля, необходимые индексы
CREATE TABLE IF NOT EXISTS orders
(
    id                  UUID PRIMARY KEY DEFAULT uuidv7(),
    address             TEXT
);