package com.huynh.personal_expense_be.modules.budget.infrastructure.persistence;

import com.huynh.personal_expense_be.modules.budget.application.port.out.BudgetPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Repository
@RequiredArgsConstructor
public class BudgetPersistenceAdapter implements BudgetPersistencePort {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void ensureBudgetExists(String userId, String categoryId, String period) {
        String previousPeriod = YearMonth.parse(period, DateTimeFormatter.ofPattern("yyyy-MM"))
                .minusMonths(1).toString();

        String sql = """
            INSERT INTO budgets (user_id, category_id, period, limit_amount, spent_amount, status)
            SELECT 
                :userId, 
                :categoryId, 
                :period, 
                COALESCE((SELECT limit_amount FROM budgets 
                          WHERE user_id = :userId 
                            AND category_id = :categoryId 
                            AND period = :previousPeriod), 0), 
                0, 
                'ACTIVE'
            ON CONFLICT (user_id, category_id, period) DO NOTHING
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("categoryId", categoryId)
                .addValue("period", period)
                .addValue("previousPeriod", previousPeriod);

        jdbcTemplate.update(sql, params);
    }

    @Override
    public void incrementSpentAmount(String userId, String categoryId, String period, BigDecimal delta) {
        String sql = """
            UPDATE budgets
            SET spent_amount = spent_amount + :delta
            WHERE user_id = :userId
              AND category_id = :categoryId
              AND period = :period
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("categoryId", categoryId)
                .addValue("period", period)
                .addValue("delta", delta);

        jdbcTemplate.update(sql, params);
        
        // Prevent negative spentAmount mechanism
        String fixNegativeSql = """
            UPDATE budgets
            SET spent_amount = 0
            WHERE user_id = :userId
              AND category_id = :categoryId
              AND period = :period
              AND spent_amount < 0
            """;
        jdbcTemplate.update(fixNegativeSql, params);
    }
}
