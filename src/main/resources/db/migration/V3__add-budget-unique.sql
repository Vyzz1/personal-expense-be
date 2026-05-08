ALTER TABLE budgets
    ADD CONSTRAINT uk_user_period_category UNIQUE (user_id, period, category_id);
