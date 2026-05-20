package com.huynh.personal_expense_be.modules.budget.infrastructure.persistence;

import com.huynh.personal_expense_be.modules.budget.application.port.out.BudgetPersistencePort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class BudgetPersistenceAdapter implements BudgetPersistencePort {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public void ensureBudgetExists(String userId, UUID categoryId, String period) {
        String previousPeriod = YearMonth.parse(period, DateTimeFormatter.ofPattern("yyyy-MM"))
                .minusMonths(1).toString();

        log.info("Ensuring budget exists for userId={}, categoryId={}, period={}", userId, categoryId, period);

        String sql = """
            INSERT INTO budgets (id, user_id, name, category_id, period, limit_amount, spent_amount, budget_status, created_at, updated_at)
            SELECT 
                ?,
                b.user_id, 
                b.name,
                b.category_id, 
                ?, 
                b.limit_amount, 
                0, 
                'ACTIVE',
                 CURRENT_TIMESTAMP,
                 CURRENT_TIMESTAMP
            FROM budgets b
            WHERE b.user_id = ? 
              AND b.category_id = ? 
              AND b.period = ?
            ON CONFLICT (user_id, category_id, period) DO NOTHING
            """;

        entityManager.unwrap(Session.class).doWork(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setObject(1, UUID.randomUUID());
                pstmt.setString(2, period);
                pstmt.setString(3, userId);
                pstmt.setObject(4, categoryId);
                pstmt.setString(5, previousPeriod);
                pstmt.executeUpdate();
            }
        });
    }

    @Override
    public int incrementSpentAmount(String userId, UUID categoryId, String period, BigDecimal delta) {
        String sql = """
        UPDATE budgets
        SET spent_amount = GREATEST(spent_amount + ?, 0)
        WHERE user_id = ?
          AND category_id = ?
          AND period = ?
        """;

        int[] result = new int[1];
        entityManager.unwrap(Session.class).doWork(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setBigDecimal(1, delta);
                pstmt.setString(2, userId);
                pstmt.setObject(3, categoryId);
                pstmt.setString(4, period);
                result[0] = pstmt.executeUpdate();
            }
        });

        return result[0];
    }

    @Override
    public int markBudgetExceededIfThresholdReached(String userId, UUID categoryId, String period) {
        String sql = """
        UPDATE budgets
        SET budget_status = 'EXCEEDED',
            updated_at = CURRENT_TIMESTAMP
        WHERE user_id = ?
          AND category_id = ?
          AND period = ?
          AND budget_status <> 'EXPIRED'
          AND spent_amount >= (
                limit_amount * (
                    CASE
                        WHEN threshold_percentage > 1 THEN threshold_percentage / 100.0
                        ELSE threshold_percentage
                    END
                )
            )
        """;

        int[] result = new int[1];
        entityManager.unwrap(Session.class).doWork(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, userId);
                pstmt.setObject(2, categoryId);
                pstmt.setString(3, period);
                result[0] = pstmt.executeUpdate();
            }
        });
        return result[0];
    }

    @Override
    public int expireBudgetsBeforePeriod(String period) {
        String sql = """
        UPDATE budgets
        SET budget_status = 'EXPIRED',
            updated_at = CURRENT_TIMESTAMP
        WHERE period < ?
          AND budget_status <> 'EXPIRED'
        """;

        int[] result = new int[1];
        entityManager.unwrap(Session.class).doWork(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, period);
                result[0] = pstmt.executeUpdate();
            }
        });
        return result[0];
    }
}


