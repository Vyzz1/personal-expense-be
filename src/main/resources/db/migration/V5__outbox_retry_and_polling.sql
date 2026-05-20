ALTER TABLE outbox_messages
    ADD COLUMN attempt_count INTEGER NOT NULL DEFAULT 0;

ALTER TABLE outbox_messages
    ADD COLUMN next_retry_at TIMESTAMP WITHOUT TIME ZONE;

CREATE INDEX idx_outbox_messages_polling
    ON outbox_messages (processed_at, next_retry_at, created_at);
