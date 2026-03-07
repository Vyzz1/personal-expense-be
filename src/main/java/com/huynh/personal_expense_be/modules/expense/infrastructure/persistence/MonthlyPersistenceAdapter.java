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
                .createQuery("SELECT m FROM MonthlyExpenseJpaEntity m WHERE m.userId = :userId AND m.month = :month AND m.year = :year AND m.isDeleted IS NULL ", MonthlyExpenseJpaEntity.class)
                .setParameter("userId", userId)
                .setParameter("month", month)
                .setParameter("year", year)
                .setMaxResults(1)
                .getResultList();

        MonthlyExpenseJpaEntity entity = results.isEmpty() ? null : results.get(0);

        if (entity == null) return null;

        return monthlyExpenseMapper.toDomain(entity);
    }

    @Transactional(readOnly = true)
    @Override
    public List<MonthlyExpense> findThreeMonthCompare(String userId, int month, int year) {
        // Build list of (month, year) for current and 2 preceding months
        List<Integer> months = new java.util.ArrayList<>();
        List<Integer> years = new java.util.ArrayList<>();
        int m = month, y = year;
        for (int i = 0; i < 3; i++) {
            months.add(m);
            years.add(y);
            m--;
            if (m == 0) {
                m = 12;
                y--;
            }
        }

        List<MonthlyExpenseJpaEntity> results = entityManager
                .createQuery(
                        "SELECT me FROM MonthlyExpenseJpaEntity me " +
                        "WHERE me.userId = :userId " +
                        "AND me.isDeleted IS NULL " +
                        "AND ((me.month = :m0 AND me.year = :y0) " +
                        "  OR (me.month = :m1 AND me.year = :y1) " +
                        "  OR (me.month = :m2 AND me.year = :y2)) " +
                        "ORDER BY me.year DESC, me.month DESC",
                        MonthlyExpenseJpaEntity.class)
                .setParameter("userId", userId)
                .setParameter("m0", months.get(0)).setParameter("y0", years.get(0))
                .setParameter("m1", months.get(1)).setParameter("y1", years.get(1))
                .setParameter("m2", months.get(2)).setParameter("y2", years.get(2))
                .getResultList();

        return results.stream()
                .map(monthlyExpenseMapper::toDomain)
                .collect(java.util.stream.Collectors.toList());
    }
}
