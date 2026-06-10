ALTER TABLE categories
    ADD CONSTRAINT uk_categories_user_id_name UNIQUE (user_id, name);
