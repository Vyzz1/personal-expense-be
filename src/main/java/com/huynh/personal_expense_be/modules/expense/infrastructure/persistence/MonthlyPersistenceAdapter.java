package com.huynh.personal_expense_be.modules.expense.infrastructure.persistence;

import com.huynh.personal_expense_be.modules.expense.application.port.out.MonthlyExpenseRepositoryPort;
import com.huynh.personal_expense_be.modules.expense.domain.MonthlyExpense;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlyPersistenceAdapter implements MonthlyExpenseRepositoryPort {

    @PersistenceContext
    private EntityManager entityManager;

    private final MonthlyExpenseMapper monthlyExpenseMapper;

    @Transactional
    @Override
    public MonthlyExpense saveMonthlyExpense(MonthlyExpense monthlyExpense) {
        MonthlyExpenseJpaEntity entity = monthlyExpenseMapper.toJpaEntity(monthlyExpense);
        MonthlyExpenseJpaEntity saved = entityManager.merge(entity);
        return monthlyExpenseMapper.toDomain(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public MonthlyExpense findByUserIdAndMonth(String userId, int month, int year) {
        log.info("Finding MonthlyExpense for userId: {}, month: {}, year: {}", userId, month, year);
        List<MonthlyExpenseJpaEntity> results = entityManager
                .createQuery("SELECT m FROM MonthlyExpenseJpaEntity m WHERE m.userId = :userId AND m.month = :month AND m.year = :year", MonthlyExpenseJpaEntity.class)
                .setParameter("userId", userId)
                .setParameter("month", month)
                .setParameter("year", year)
                .setMaxResults(1)
                .getResultList();

        MonthlyExpenseJpaEntity entity = results.isEmpty() ? null : results.get(0);

        if (entity == null) return null;

        return monthlyExpenseMapper.toDomain(entity);
    }
}
