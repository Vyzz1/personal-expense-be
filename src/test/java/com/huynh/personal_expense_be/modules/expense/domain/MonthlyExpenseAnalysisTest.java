package com.huynh.personal_expense_be.modules.expense.domain;
import java.util.UUID;

import org.junit.jupiter.api.Test;

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

        assert analysis.getTotalAmount().equals(java.math.BigDecimal.valueOf(1500));
        assert analysis.getPreviousTotalAmount().equals(java.math.BigDecimal.valueOf(1200));
        assert analysis.getChangePercentage().equals(java.math.BigDecimal.valueOf(25.0));
    }

    
}
