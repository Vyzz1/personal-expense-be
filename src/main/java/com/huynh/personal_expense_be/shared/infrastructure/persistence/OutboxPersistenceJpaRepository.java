package com.huynh.personal_expense_be.shared.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxPersistenceJpaRepository extends JpaRepository<OutboxMessageJpaEntity, UUID> {

    @Query(value = """
            SELECT *
            FROM outbox_messages
            WHERE processed_at IS NULL
              AND (next_retry_at IS NULL OR next_retry_at <= NOW())
            ORDER BY created_at ASC
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxMessageJpaEntity> pollProcessableMessages(int limit);
}
