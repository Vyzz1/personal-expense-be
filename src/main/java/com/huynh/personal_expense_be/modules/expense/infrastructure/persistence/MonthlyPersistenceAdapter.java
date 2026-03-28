package com.huynh.personal_expense_be.modules.expense.infrastructure.persistence;

import com.huynh.personal_expense_be.modules.expense.application.port.out.MonthlyExpenseRepositoryPort;
import com.huynh.personal_expense_be.modules.expense.domain.MonthlyExpense;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlyPersistenceAdapter implements MonthlyExpenseRepositoryPort {

    private static final String UPSERT_SQL = """
            INSERT INTO monthly_expenses
            (id, user_id, month, year, total_amount, previous_total_amount, change_percentage, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (user_id, month, year)
            DO UPDATE SET
                total_amount = monthly_expenses.total_amount + EXCLUDED.total_amount,
                previous_total_amount = EXCLUDED.previous_total_amount,
                change_percentage = EXCLUDED.change_percentage
            """;

    @PersistenceContext
    private EntityManager entityManager;

    private final MonthlyExpenseMapper monthlyExpenseMapper;

    private final MonthlyPersistenceAdapter self; 

    @Transactional
    @Override
    public MonthlyExpense saveMonthlyExpense(MonthlyExpense monthlyExpense) {
        return this.self.saveAllMonthlyExpenses(List.of(monthlyExpense)).get(0);
    }

    @Transactional
    @Override
    public List<MonthlyExpense> saveAllMonthlyExpenses(List<MonthlyExpense> monthlyExpenses) {
        if (monthlyExpenses == null || monthlyExpenses.isEmpty())
            return Collections.emptyList();

        List<UUID> ids = monthlyExpenses.stream()
                .map(me -> me.getId() != null ? me.getId() : UUID.randomUUID())
                .collect(Collectors.toList());

        Session session = entityManager.unwrap(Session.class);
        session.doWork(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(UPSERT_SQL)) {
                for (int i = 0; i < monthlyExpenses.size(); i++) {
                    MonthlyExpense me = monthlyExpenses.get(i);
                    ps.setObject(1, ids.get(i));
                    ps.setString(2, me.getUserId());
                    ps.setInt(3, me.getMonth());
                    ps.setInt(4, me.getYear());
                    ps.setBigDecimal(5, me.getTotalAmount());
                    ps.setBigDecimal(6, me.getPreviousTotalAmount());
                    ps.setBigDecimal(7, me.getChangePercentage());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        });

        return IntStream.range(0, monthlyExpenses.size())
                .mapToObj(i -> monthlyExpenses.get(i).toBuilder().id(ids.get(i)).build())
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    @Override
    public List<MonthlyExpense> findAllByUserIdAndMonthYearPairs(String userId, List<int[]> monthYearPairs) {
        if (monthYearPairs == null || monthYearPairs.isEmpty())
            return Collections.emptyList();

        Integer[] months = monthYearPairs.stream().map(p -> p[0]).toArray(Integer[]::new);
        Integer[] years = monthYearPairs.stream().map(p -> p[1]).toArray(Integer[]::new);

        log.info("Finding MonthlyExpenses for userId: {}, monthYearPairs: {}", userId, monthYearPairs);

        String sql = "SELECT * FROM monthly_expenses " +
                "WHERE user_id = :userId " +
                "  AND is_deleted IS NULL " +
                "  AND (month, year) IN (" +
                "      SELECT * FROM unnest(CAST(:months AS int[]), CAST(:years AS int[]))" +
                "  )";

        TypedQuery<MonthlyExpenseJpaEntity> query = entityManager
                .createNativeQuery(sql, MonthlyExpenseJpaEntity.class)
                .unwrap(TypedQuery.class);

        return query
                .setParameter("userId", userId)
                .setParameter("months", months)
                .setParameter("years", years)
                .getResultList()
                .stream()
                .map(monthlyExpenseMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public MonthlyExpense findByUserIdAndMonth(String userId, int month, int year) {
        log.info("Finding MonthlyExpense for userId: {}, month: {}, year: {}", userId, month, year);
        List<MonthlyExpenseJpaEntity> results = entityManager
                .createQuery(
                        "SELECT m FROM MonthlyExpenseJpaEntity m WHERE m.userId = :userId AND m.month = :month AND m.year = :year AND m.isDeleted IS NULL ",
                        MonthlyExpenseJpaEntity.class)
                .setParameter("userId", userId)
                .setParameter("month", month)
                .setParameter("year", year)
                .setMaxResults(1)
                .getResultList();

        MonthlyExpenseJpaEntity entity = results.isEmpty() ? null : results.get(0);

        if (entity == null)
            return null;

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
