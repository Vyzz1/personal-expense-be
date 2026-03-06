package com.huynh.personal_expense_be.modules.transaction.infrastructure.persistence;

import com.huynh.personal_expense_be.modules.transaction.application.dto.GetTransactionCommand;
import com.huynh.personal_expense_be.modules.transaction.application.dto.PageResult;
import com.huynh.personal_expense_be.modules.transaction.application.port.out.TransactionRepositoryPort;
import com.huynh.personal_expense_be.modules.transaction.domain.Transaction;
import com.huynh.personal_expense_be.modules.transaction.domain.TransactionType;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
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

    @Override
    public PageResult<Transaction> findAllWithFilter(GetTransactionCommand command) {
        StringBuilder where = getWhereQuery(command);

        String sortBy = (command.sortBy() != null && !command.sortBy().isBlank()) ? command.sortBy() : "occurredAt";
        String sortDir = (command.sortOrder() != null && command.sortOrder().equalsIgnoreCase("asc")) ? "ASC" : "DESC";

        TypedQuery<TransactionJpaEntity> dataQuery = entityManager.createQuery(
                "SELECT t FROM TransactionJpaEntity t JOIN FETCH t.category " + where + " ORDER BY t." + sortBy + " " + sortDir,
                TransactionJpaEntity.class);

        TypedQuery<Long> countQuery = entityManager.createQuery(
                "SELECT COUNT(t) FROM TransactionJpaEntity t " + where,
                Long.class);

        dataQuery.setParameter("userId", command.userId());
        countQuery.setParameter("userId", command.userId());

        if (command.description() != null && !command.description().isBlank()) {
            dataQuery.setParameter("description", "%" + command.description() + "%");
            countQuery.setParameter("description", "%" + command.description() + "%");
        }
        if (command.categoryIds() != null && !command.categoryIds().isEmpty()) {
            dataQuery.setParameter("categoryIds", command.categoryIds());
            countQuery.setParameter("categoryIds", command.categoryIds());
        }
        if (command.type() != null && !command.type().isBlank()) {
            TransactionType type = TransactionType.valueOf(command.type().toUpperCase());
            dataQuery.setParameter("type", type);
            countQuery.setParameter("type", type);
        }
        if (command.fromDate() != null && !command.fromDate().isBlank()) {
            dataQuery.setParameter("fromDate", Instant.parse(command.fromDate()));
            countQuery.setParameter("fromDate", Instant.parse(command.fromDate()));
        }
        if (command.toDate() != null && !command.toDate().isBlank()) {
            dataQuery.setParameter("toDate", Instant.parse(command.toDate()));
            countQuery.setParameter("toDate", Instant.parse(command.toDate()));
        }

        int page = command.page();
        int size = command.size() > 0 ? command.size() : 10;
        dataQuery.setFirstResult(page * size);
        dataQuery.setMaxResults(size);

        long totalElements = countQuery.getSingleResult();
        List<Transaction> content = dataQuery.getResultList().stream()
                .map(transactionMapper::toDomain)
                .toList();

        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
        boolean isLast = (page + 1) >= totalPages;

        return PageResult.of(content, page, size, totalElements, totalPages, isLast);
    }

    private static StringBuilder getWhereQuery(GetTransactionCommand command) {
        StringBuilder where = new StringBuilder("WHERE t.userId = :userId AND t.isDeleted IS NULL");

        if (command.description() != null && !command.description().isBlank()) {
            where.append(" AND LOWER(t.description) LIKE LOWER(:description)");
        }
        if (command.categoryIds() != null && !command.categoryIds().isEmpty()) {
            where.append(" AND t.category.id IN :categoryIds");
        }
        if (command.type() != null && !command.type().isBlank()) {
            where.append(" AND t.type = :type");
        }
        if (command.fromDate() != null && !command.fromDate().isBlank()) {
            where.append(" AND t.occurredAt >= :fromDate");
        }
        if (command.toDate() != null && !command.toDate().isBlank()) {
            where.append(" AND t.occurredAt <= :toDate");
        }
        return where;
    }

}
