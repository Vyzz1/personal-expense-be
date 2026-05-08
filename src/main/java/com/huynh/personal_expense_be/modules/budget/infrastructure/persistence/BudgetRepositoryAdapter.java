package com.huynh.personal_expense_be.modules.budget.infrastructure.persistence;

import com.huynh.personal_expense_be.modules.budget.application.port.out.BudgetRepositoryPort;
import com.huynh.personal_expense_be.modules.budget.domain.Budget;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Transactional
public class BudgetRepositoryAdapter implements BudgetRepositoryPort {

    @PersistenceContext
    private final EntityManager entityManager;
    private final BudgetMapper budgetMapper;

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
}
