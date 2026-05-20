CREATE TABLE budgets
(
    id         UUID         NOT NULL,
    is_deleted TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    name       VARCHAR(255) NOT NULL,
    user_id    VARCHAR(255) NOT NULL,
    limit_amount DECIMAL(19, 4) NOT NULL,
    spent_amount DECIMAL(19, 4) NOT NULL,
    category_id  UUID,
    budget_status VARCHAR(50) NOT NULL,
    period VARCHAR(7) NOT NULL,
    CONSTRAINT fk_budgets_category
        FOREIGN KEY (category_id)
            REFERENCES categories(id),
    CONSTRAINT pk_budgets PRIMARY KEY (id)
);

