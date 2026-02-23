package com.huynh.personal_expense_be.modules.transaction.infrastructure.persistence;

import com.huynh.personal_expense_be.modules.transaction.application.port.out.TransactionRepositoryPort;
import com.huynh.personal_expense_be.modules.transaction.domain.Transaction;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionPersistenceAdapter implements TransactionRepositoryPort {

    @PersistenceContext
    private EntityManager entityManager;
    private final TransactionMapper transactionMapper;

    @Transactional
    @Override
    public Transaction save(Transaction transaction) {
        TransactionJpaEntity entity = transactionMapper.toJpaEntity(transaction);
        TransactionJpaEntity saved = entityManager.merge(entity);
        return transactionMapper.toDomain(saved);
    }

    @Override
    public List<Transaction> findByUserId(String userId) {

        log.info("Finding transactions for userId: {}", userId);
        List<TransactionJpaEntity> entities = entityManager
                .createQuery("SELECT t FROM TransactionJpaEntity t JOIN FETCH t.category" +
                        " WHERE t.userId = :userId AND t.isDeleted IS NULL", TransactionJpaEntity.class)
                .setParameter("userId", userId)
                .getResultList();

        return  entities.stream()
                .map(transactionMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Transaction> findById(UUID id) {
        TransactionJpaEntity entity = entityManager
                .createQuery("SELECT t FROM TransactionJpaEntity t JOIN FETCH t.category" +
                        " WHERE t.id = :id AND t.isDeleted IS NULL", TransactionJpaEntity.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (entity == null) {
            return Optional.empty();
        }

        return Optional.of(transactionMapper.toDomain(entity));
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        Instant now = Instant.now();
        entityManager.createQuery("UPDATE TransactionJpaEntity t SET t.isDeleted = :now WHERE t.id = :id")
                .setParameter("id", id)
                .setParameter("now", now)
                .executeUpdate();

    }




}
