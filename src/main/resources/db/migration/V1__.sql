CREATE TABLE categories
(
    id         UUID         NOT NULL,
    is_deleted TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    name       VARCHAR(255) NOT NULL,
    user_id    VARCHAR(255) NOT NULL,
    parent_id  UUID,
    CONSTRAINT pk_categories PRIMARY KEY (id)
);

CREATE TABLE monthly_expenses
(
    id           UUID           NOT NULL,
    is_deleted   TIMESTAMP WITHOUT TIME ZONE,
    created_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    user_id      VARCHAR(255)   NOT NULL,
    month        INTEGER        NOT NULL,
    year         INTEGER        NOT NULL,
    total_amount DECIMAL(19, 4) NOT NULL,
    CONSTRAINT pk_monthly_expenses PRIMARY KEY (id)
);

CREATE TABLE outbox_messages
(
    id           UUID         NOT NULL,
    module       VARCHAR(100) NOT NULL,
    payload      TEXT         NOT NULL,
    created_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITHOUT TIME ZONE,
    error        VARCHAR(1000),
    event_type   VARCHAR(255) NOT NULL,
    CONSTRAINT pk_outbox_messages PRIMARY KEY (id)
);

CREATE TABLE transactions
(
    id          UUID           NOT NULL,
    is_deleted  TIMESTAMP WITHOUT TIME ZONE,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    description TEXT,
    amount      DECIMAL(19, 4) NOT NULL,
    user_id     VARCHAR(255)   NOT NULL,
    category_id UUID           NOT NULL,
    occurred_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    type        VARCHAR(255)   NOT NULL,
    CONSTRAINT pk_transactions PRIMARY KEY (id)
);

ALTER TABLE monthly_expenses
    ADD CONSTRAINT uk_monthly_expenses_user_month_year UNIQUE (user_id, month, year);

ALTER TABLE transactions
    ADD CONSTRAINT FK_TRANSACTIONS_ON_CATEGORY FOREIGN KEY (category_id) REFERENCES categories (id);