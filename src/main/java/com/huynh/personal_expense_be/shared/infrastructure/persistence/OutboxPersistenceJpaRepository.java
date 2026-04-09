    package com.huynh.personal_expense_be.shared.infrastructure.persistence;

    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;

    import java.util.List;
    import java.util.UUID;

    @Repository
    public interface OutboxPersistenceJpaRepository extends JpaRepository<OutboxMessageJpaEntity, UUID> {

        List<OutboxMessageJpaEntity> findByProcessedAtIsNull();
    }
