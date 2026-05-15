package com.huynh.personal_expense_be.modules.expense.domain;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class MonthlyExpenseAnalysisTest {
    
    
    @Test
    public void shouldCreateMonthlyExpenseAnalysis() {
        MonthlyExpenseAnalysis analysis = MonthlyExpenseAnalysis.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID().toString())
                .month(5)
                .year(2024)
                .totalAmount(java.math.BigDecimal.valueOf(1500))
                .previousTotalAmount(java.math.BigDecimal.valueOf(1200))
                .changePercentage(java.math.BigDecimal.valueOf(25.0))
                .build();

        assertThat(analysis.getTotalAmount())
                .isEqualByComparingTo("1500");

        assertThat(analysis.getPreviousTotalAmount())
                .isEqualByComparingTo("1200");

        assertThat(analysis.getChangePercentage())
                .isEqualByComparingTo("25.0");
    }

    
}
