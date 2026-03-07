package com.huynh.personal_expense_be.modules.expense.application.dto;

import com.huynh.personal_expense_be.modules.expense.domain.MonthlyExpense;

import java.math.BigDecimal;
import java.time.Instant;

public record GetMonthlyExpenseResponse(
         BigDecimal totalAmount,
         BigDecimal previousTotalAmount,
         BigDecimal changePercentage,
         Instant lastCalculatedAt,
         int month,
         int year
) {

    public static GetMonthlyExpenseResponse of(MonthlyExpense monthlyExpense) {
        return new GetMonthlyExpenseResponse(
                monthlyExpense.getTotalAmount(),
                monthlyExpense.getPreviousTotalAmount(),
                monthlyExpense.getChangePercentage(),
                monthlyExpense.getUpdatedAt(),
                monthlyExpense.getMonth(),
                monthlyExpense.getYear()
        );
    }
}
