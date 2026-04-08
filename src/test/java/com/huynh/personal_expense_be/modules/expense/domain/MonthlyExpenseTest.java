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
                .totalAmount(BigDecimal.valueOf(1000))
                .isDeleted(null)
                .build();

        assertEquals(BigDecimal.valueOf(1000), expense.getTotalAmount());
        assertEquals(null, expense.getIsDeleted());

    }

   
    
}
