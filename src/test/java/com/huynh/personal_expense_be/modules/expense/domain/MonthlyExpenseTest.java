package com.huynh.personal_expense_be.modules.expense.domain;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;

public class MonthlyExpenseTest {

    @Test
    void shouldCreateMonthlyExpense() {
        MonthlyExpense expense = MonthlyExpense.builder()
                .id(java.util.UUID.randomUUID())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .previousTotalAmount(BigDecimal.valueOf(500))
                .totalAmount(BigDecimal.valueOf(1000))
                .changePercentage(BigDecimal.valueOf(100))
                .isDeleted(null)
                .build();

        assertEquals(BigDecimal.valueOf(1000), expense.getTotalAmount());
        assertEquals(BigDecimal.valueOf(500), expense.getPreviousTotalAmount());
        assertEquals(BigDecimal.valueOf(100), expense.getChangePercentage());
        assertEquals(null, expense.getIsDeleted());

    }

    @Test 
    void shouldUpdateTotalAmount() {
        MonthlyExpense expense = MonthlyExpense.builder()
                .totalAmount(BigDecimal.valueOf(1000))
                .build();

        expense.updateTotalAmount(BigDecimal.valueOf(500));

        assertEquals(0, expense.getTotalAmount().compareTo(BigDecimal.valueOf(1500)));
        assertEquals(0, expense.getChangePercentage().compareTo(BigDecimal.valueOf(50)));
    }

    @Test
    void shouldDeductAmount() {
        MonthlyExpense expense = MonthlyExpense.builder()
                .totalAmount(BigDecimal.valueOf(1000))
                .build();

        expense.deductAmount(BigDecimal.valueOf(200));

        assertEquals(0, expense.getTotalAmount().compareTo(BigDecimal.valueOf(800)));
        assertEquals(0, expense.getChangePercentage().compareTo(BigDecimal.valueOf(-20)));
    }

    @Test
    void shouldHandleZeroPreviousTotalAmount() {
        MonthlyExpense expense = MonthlyExpense.builder()
                .totalAmount(BigDecimal.valueOf(1000))
                .build();

        MonthlyExpense previousExpense = MonthlyExpense.builder()
                .totalAmount(BigDecimal.ZERO)
                .build();

        expense.withPrevious(previousExpense);

        assertEquals(0, expense.getChangePercentage().compareTo(BigDecimal.ZERO));
    }

    @Test
    void shouldHandleNullPreviousExpense() {
        MonthlyExpense expense = MonthlyExpense.builder()
                .totalAmount(BigDecimal.valueOf(1000))
                .build();

        expense.withPrevious(null);

        assertEquals(null, expense.getPreviousTotalAmount());
        assertEquals(null, expense.getChangePercentage());
    }

    @Test
    void shouldHandleZeroTotalAmount() {
        MonthlyExpense expense = MonthlyExpense.builder()
                .totalAmount(BigDecimal.ZERO)
                .build();

        expense.updateTotalAmount(BigDecimal.valueOf(500));

        assertEquals(0, expense.getTotalAmount().compareTo(BigDecimal.valueOf(500)));
        assertEquals(0, expense.getChangePercentage().compareTo(BigDecimal.ZERO));
    }
    
}
