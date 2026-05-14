package com.huynh.personal_expense_be.modules.budget.infrastructure.persistence;

import com.huynh.personal_expense_be.modules.budget.application.dto.GetListBudgetCommand;
import com.huynh.personal_expense_be.modules.budget.application.port.out.BudgetRepositoryPort;
import com.huynh.personal_expense_be.modules.budget.domain.Budget;
import com.huynh.personal_expense_be.modules.transaction.application.dto.PageResult;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
@Transactional
public class BudgetRepositoryAdapter implements BudgetRepositoryPort {

    @PersistenceContext
    private final EntityManager entityManager;
    private final BudgetMapper budgetMapper;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "period",
            "createdAt",
            "updatedAt",
            "limitAmount",
            "spentAmount",
            "status",
            "thresholdPercentage"
    );

    @Override
    public Budget save(Budget budget) {
        BudgetJpaEntity entity = budgetMapper.toEntity(budget);
        if (entity.getId() == null) {
            entityManager.persist(entity);
            return budgetMapper.toDomain(entity);
        } else {
            BudgetJpaEntity mergedEntity = entityManager.merge(entity);
            return budgetMapper.toDomain(mergedEntity);
        }
    }

    @Override
    public Optional<Budget> findById(String userId, UUID budgetId) {
        try {
            BudgetJpaEntity entity = entityManager.createQuery(
                            "SELECT b FROM BudgetJpaEntity b WHERE b.userId = :userId AND b.id = :id", BudgetJpaEntity.class)
                    .setParameter("userId", userId)
                    .setParameter("id", budgetId)
                    .getSingleResult();
            return Optional.of(budgetMapper.toDomain(entity));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteById(UUID budgetId) {
        BudgetJpaEntity entity = entityManager.find(BudgetJpaEntity.class, budgetId);
        if (entity != null) {
            entityManager.remove(entity);
        }
    }

    @Override
    public List<Budget> findByUserIdAndPeriod(String userId, String period) {
        YearMonth yearMonth = YearMonth.parse(period, DateTimeFormatter.ofPattern("yyyy-MM"));
        return entityManager.createQuery(
                        "SELECT b FROM BudgetJpaEntity b WHERE b.userId = :userId AND b.period = :period", BudgetJpaEntity.class)
                .setParameter("userId", userId)
                .setParameter("period", yearMonth.toString())
                .getResultList()
                .stream()
                .map(budgetMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByUserId(String userId) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(b) FROM BudgetJpaEntity b WHERE b.userId = :userId AND b.category IS  NULL ", Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
        return count > 0;
    }

    @Override
    public List<Budget> findByUserId(String userId) {
        return entityManager.createQuery(
                        "SELECT b FROM BudgetJpaEntity b WHERE b.userId = :userId", BudgetJpaEntity.class)
                .setParameter("userId", userId)
                .getResultList()
                .stream()
                .map(budgetMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsOverallByUserId(String userId) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(b) FROM BudgetJpaEntity b WHERE b.userId = :userId AND  b.category IS NULL ", Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
        return count > 0;
    }

    @Override
    public boolean existsByCategoryIdAndUserId(UUID categoryId, String userId) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(b) FROM BudgetJpaEntity b WHERE b.category.id = :categoryId AND b.userId = :userId", Long.class)
                .setParameter("categoryId", categoryId)
                .setParameter("userId", userId)
                .getSingleResult();
        return count > 0;
    }

    @Override
    public PageResult<Budget> findAllWithFilter(GetListBudgetCommand command) {

        String where = getWhereQuery(command);

        String sortBy = "period";

        if (command.sortBy() != null
                && ALLOWED_SORT_FIELDS.contains(command.sortBy())) {
            sortBy = command.sortBy();
        }

        String sortDir = (command.sortOrder() != null && command.sortOrder().equalsIgnoreCase("asc")) ? "ASC" : "DESC";

        TypedQuery<BudgetJpaEntity> dataQuery = entityManager.createQuery(
                "SELECT b FROM BudgetJpaEntity b LEFT JOIN FETCH b.category " + where + " ORDER BY b." + sortBy + " "
                        + sortDir,
                BudgetJpaEntity.class);

        TypedQuery<Long> countQuery = entityManager.createQuery(
                "SELECT COUNT(b) FROM BudgetJpaEntity b " + where,
                Long.class);

        dataQuery.setParameter("userId", command.userId());
        countQuery.setParameter("userId", command.userId());

        if (command.categoryIds() != null && !command.categoryIds().isEmpty()) {
            dataQuery.setParameter("categoryIds", command.categoryIds());
            countQuery.setParameter("categoryIds", command.categoryIds());
        }

        if (command.search() != null && !command.search().isBlank()) {
            log.info("Applying search filter with value: {}", command.search());
            dataQuery.setParameter("search", "%" + command.search() + "%");
            countQuery.setParameter("search", "%" + command.search() + "%");
        }

        if (command.period() != null) {
            dataQuery.setParameter("period", command.period().toString());
            countQuery.setParameter("period", command.period().toString());
        }

        if (command.status() != null) {
            dataQuery.setParameter("status", command.status());
            countQuery.setParameter("status", command.status());
        }


        int page = command.page();
        int size = command.size() > 0 ? command.size() : 10;
        dataQuery.setFirstResult(page * size);
        dataQuery.setMaxResults(size);

        long totalElements = countQuery.getSingleResult();
        List<Budget> content = dataQuery.getResultList().stream()
                .map(budgetMapper::toDomain)
                .toList();

        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
        boolean isLast = (page + 1) >= totalPages;

        return PageResult.of(content, page, size, totalElements, totalPages, isLast);

    }

    private String getWhereQuery(GetListBudgetCommand command) {
        StringBuilder where = new StringBuilder(" WHERE b.userId = :userId");

        if (command.categoryIds() != null && !command.categoryIds().isEmpty()) {
            where.append(" AND b.category.id IN :categoryIds");
        }

        if (command.search() != null && !command.search().isBlank()) {
            where.append(" AND LOWER(b.name) LIKE LOWER(:search)");
        }

        if (command.period() != null) {
            where.append(" AND b.period = :period");

        }
        if (command.status() != null) {
            where.append(" AND b.status = :status");
        }

        return where.toString();
    }
}
