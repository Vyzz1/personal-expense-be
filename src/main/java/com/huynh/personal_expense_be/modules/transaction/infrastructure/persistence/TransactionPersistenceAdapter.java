package com.huynh.personal_expense_be.modules.transaction.infrastructure.persistence;

import com.huynh.personal_expense_be.modules.transaction.application.dto.GetTransactionCommand;
import com.huynh.personal_expense_be.modules.transaction.application.dto.PageResult;
import com.huynh.personal_expense_be.modules.transaction.application.port.out.TransactionRepositoryPort;
import com.huynh.personal_expense_be.modules.transaction.domain.Transaction;
import com.huynh.personal_expense_be.modules.transaction.domain.TransactionType;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionPersistenceAdapter implements TransactionRepositoryPort {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "occurredAt", "amount", "createdAt", "updatedAt", "description", "type"
    );

    @PersistenceContext
    private EntityManager entityManager;
    private final TransactionMapper transactionMapper;
    private final TransactionJpaRepository transactionJpaRepository;

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

        return entities.stream()
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
        String sortBy = (command.sortBy() != null && ALLOWED_SORT_FIELDS.contains(command.sortBy())) ? command.sortBy() : "occurredAt";
        Sort.Direction direction = "asc".equalsIgnoreCase(command.sortOrder()) ? Sort.Direction.ASC : Sort.Direction.DESC;

        int size = command.size() > 0 ? command.size() : 10;
        PageRequest pageable = PageRequest.of(command.page(), size, Sort.by(direction, sortBy));

        Specification<TransactionJpaEntity> spec = buildSpec(command);

        log.debug("Min & max amount {} {}", command.minAmount(), command.maxAmount());

        Page<TransactionJpaEntity> result = transactionJpaRepository.findAll(spec, pageable);

        List<Transaction> content = result.getContent().stream()
                .map(transactionMapper::toDomain)
                .toList();

        return PageResult.of(content, command.page(), size, result.getTotalElements(),
                result.getTotalPages(), result.isLast());
    }

    private Specification<TransactionJpaEntity> buildSpec(GetTransactionCommand command) {
        ZoneId zone = ZoneId.systemDefault();

        Specification<TransactionJpaEntity> spec = TransactionSpecification.notDeleted()
                .and(TransactionSpecification.byUserId(command.userId()))
                .and(TransactionSpecification.withCategory());

        if (command.description() != null && !command.description().isBlank()) {
            spec = spec.and(TransactionSpecification.descriptionContains(command.description()));
        }
        if (command.categoryIds() != null && !command.categoryIds().isEmpty()) {
            spec = spec.and(TransactionSpecification.inCategories(command.categoryIds()));
        }
        if (command.type() != null && !command.type().isEmpty()) {
            List<TransactionType> types = command.type().stream()
                    .map(String::toUpperCase)
                    .map(TransactionType::valueOf)
                    .toList();
            spec = spec.and(TransactionSpecification.byTypes(types));
        }
        if (command.fromDate() != null && !command.fromDate().isBlank()) {
            Instant from = LocalDate.parse(command.fromDate()).atStartOfDay(zone).toInstant();
            spec = spec.and(TransactionSpecification.fromDate(from));
        }
        if (command.toDate() != null && !command.toDate().isBlank()) {
            Instant to = LocalDate.parse(command.toDate()).atTime(LocalTime.MAX).atZone(zone).toInstant();
            spec = spec.and(TransactionSpecification.toDate(to));
        }
        if (command.month() > 0 && command.year() > 0) {
            spec = spec.and(TransactionSpecification.byMonthYear(command.month(), command.year()));
        }
        if (command.minAmount() != null) {
            spec = spec.and(TransactionSpecification.minAmount(command.minAmount()));
        }
        if (command.maxAmount() != null) {
            spec = spec.and(TransactionSpecification.maxAmount(command.maxAmount()));
        }

        return spec;
    }

}
