ALTER TABLE outbox_messages
    ADD COLUMN retry_count INTEGER NOT NULL DEFAULT 0;
