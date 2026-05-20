package com.huynh.personal_expense_be.modules.expense.infrastructure.persistence;

import com.huynh.personal_expense_be.modules.expense.application.port.out.MonthlyExpenseRepositoryPort;
import com.huynh.personal_expense_be.modules.expense.domain.MonthlyExpense;
import com.huynh.personal_expense_be.modules.expense.domain.MonthlyExpenseAnalysis;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
            (id, user_id, month, year, total_amount, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (user_id, month, year)
            DO UPDATE SET
                total_amount = monthly_expenses.total_amount + EXCLUDED.total_amount,
                updated_at = CURRENT_TIMESTAMP
            """;

    @PersistenceContext
    private EntityManager entityManager;

    private final MonthlyExpenseMapper monthlyExpenseMapper;

    @Autowired
    @Lazy
    private MonthlyPersistenceAdapter self; 

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
                .toList();
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    @Override
    public MonthlyExpenseAnalysis findByUserIdAndMonth(String userId, int month, int year) {
        log.info("Finding MonthlyExpense for userId: {}, month: {}, year: {}", userId, month, year);

        List<Tuple> rows = entityManager.createNativeQuery("""
               SELECT
                    t.id AS id,
                    t.user_id AS user_id,
                    CAST(t.month AS INTEGER) AS month,
                    CAST(t.year AS INTEGER) AS year,
                    t.total_amount AS total_amount,
                    t.updated_at AS updated_at,
                CASE 
                    WHEN t.prev_year = t.year AND t.prev_month = t.month - 1 THEN t.raw_prev_total
                    WHEN t.prev_year = t.year - 1 AND t.month = 1 AND t.prev_month = 12 THEN t.raw_prev_total
                    ELSE 0 
                END AS previous_total_amount,
                CASE
                    WHEN (CASE 
                            WHEN t.prev_year = t.year AND t.prev_month = t.month - 1 THEN t.raw_prev_total
                            WHEN t.prev_year = t.year - 1 AND t.month = 1 AND t.prev_month = 12 THEN t.raw_prev_total
                            ELSE 0 
                        END) = 0 THEN 0
                    ELSE (t.total_amount - t.raw_prev_total) / t.raw_prev_total * 100
                END AS change_percentage
            FROM (
                SELECT
                    id, user_id, month, year, total_amount, updated_at,
                    LAG(total_amount) OVER (PARTITION BY user_id ORDER BY year, month) AS raw_prev_total,
                    LAG(month) OVER (PARTITION BY user_id ORDER BY year, month) AS prev_month,
                    LAG(year) OVER (PARTITION BY user_id ORDER BY year, month) AS prev_year
                FROM monthly_expenses
                WHERE user_id = :userId
            ) t
            WHERE t.month = :month AND t.year = :year
                """, Tuple.class)
                .setParameter("userId", userId)
                .setParameter("month", month)
                .setParameter("year", year)
                .setMaxResults(1)
                .getResultList();

        if (rows.isEmpty()) {
            log.info("No MonthlyExpense found for userId: {}, month: {}, year: {}. Returning null.", userId, month, year);
            return null;
        }

        return  rows.stream()
                .map(row -> {
                    LocalDateTime updatedAt = row.get(5, LocalDateTime.class);
                   return MonthlyExpenseAnalysis.builder()
                            .id(row.get(0, UUID.class))
                            .userId(row.get(1, String.class))
                            .month(row.get(2, Integer.class))
                            .year(row.get(3, Integer.class))
                            .totalAmount(row.get(4, BigDecimal.class))
                            .updatedAt(
                                    updatedAt != null
                                            ? updatedAt.atZone(ZoneId.systemDefault()).toInstant()
                                            : null
                            )
                            . previousTotalAmount(row.get(6, BigDecimal.class))
                            .changePercentage(row.get(7, BigDecimal.class))
                            .build();
                })
                .findFirst()
                .orElse(null);
    }

    @Transactional(readOnly = true)
    @Override
    public List<MonthlyExpenseAnalysis> findThreeMonthCompare(String userId, int month, int year) {
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

        @SuppressWarnings("unchecked")
        List<Tuple> rows = (List<Tuple>) entityManager
                .createNativeQuery("""
                         SELECT
                                t.id AS id,
                                t.user_id AS user_id,
                                CAST(t.month AS INTEGER) AS month,
                                CAST(t.year AS INTEGER) AS year,
                                t.total_amount AS total_amount,
                                t.updated_at AS updated_at,
                            CASE 
                                WHEN t.prev_year = t.year AND t.prev_month = t.month - 1 THEN t.raw_prev_total
                                WHEN t.prev_year = t.year - 1 AND t.month = 1 AND t.prev_month = 12 THEN t.raw_prev_total
                                ELSE 0 
                            END AS previous_total_amount,
                            CASE
                                WHEN (CASE 
                                        WHEN t.prev_year = t.year AND t.prev_month = t.month - 1 THEN t.raw_prev_total
                                        WHEN t.prev_year = t.year - 1 AND t.month = 1 AND t.prev_month = 12 THEN t.raw_prev_total
                                        ELSE 0 
                                    END) = 0 THEN 0
                                ELSE (t.total_amount - t.raw_prev_total) / t.raw_prev_total * 100
                            END AS change_percentage
                        FROM (
                            SELECT
                                id, user_id, month, year, total_amount, updated_at,
                                LAG(total_amount) OVER (PARTITION BY user_id ORDER BY year, month) AS raw_prev_total,
                                LAG(month) OVER (PARTITION BY user_id ORDER BY year, month) AS prev_month,
                                LAG(year) OVER (PARTITION BY user_id ORDER BY year, month) AS prev_year
                            FROM monthly_expenses
                            WHERE user_id = :userId
                        ) t
                        WHERE (t.month = :m0 AND t.year = :y0)
                           OR (t.month = :m1 AND t.year = :y1)
                           OR (t.month = :m2 AND t.year = :y2)
                        ORDER BY t.year ASC, t.month ASC
                        """, Tuple.class)
                .setParameter("userId", userId)
                .setParameter("m0", months.get(0)).setParameter("y0", years.get(0))
                .setParameter("m1", months.get(1)).setParameter("y1", years.get(1))
                .setParameter("m2", months.get(2)).setParameter("y2", years.get(2))
                .getResultList();

        return rows.stream()
                .map(row -> {
                    LocalDateTime updatedAt = row.get(5, LocalDateTime.class);

                    return MonthlyExpenseAnalysis.builder()
                            .id(row.get(0, UUID.class))
                            .userId(row.get(1, String.class))
                            .month(row.get(2, Integer.class))
                            .year(row.get(3, Integer.class))
                            .totalAmount(row.get(4, BigDecimal.class))
                            .updatedAt(
                                    updatedAt != null
                                            ? updatedAt.atZone(ZoneId.systemDefault()).toInstant()
                                            : null
                            )
                            .previousTotalAmount(row.get(6, BigDecimal.class))
                            .changePercentage(row.get(7, BigDecimal.class))
                            .build();
                })
                .collect(Collectors.toList());

    }
}
