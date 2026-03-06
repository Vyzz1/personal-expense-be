package com.huynh.personal_expense_be.modules.expense.application.dto;

import com.huynh.personal_expense_be.modules.category.application.dto.CategoryResponse;
import com.huynh.personal_expense_be.modules.expense.domain.MonthlyExpense;
import com.huynh.personal_expense_be.modules.transaction.application.dto.TransactionResponse;

import java.math.BigDecimal;
import java.util.UUID;

public record MonthlyExpenseResponse(
         UUID id,
         int month,
         int year,
         BigDecimal totalAmount,
         BigDecimal previousMonthAmount,
         BigDecimal percentageChange,
         String userId,
         CategoryResponse category,
         TransactionResponse transaction
) {

    public static MonthlyExpenseResponse from(MonthlyExpense monthlyExpense, CategoryResponse category, TransactionResponse transaction) {
        return new MonthlyExpenseResponse(
                monthlyExpense.getId(),
                monthlyExpense.getMonth(),
                monthlyExpense.getYear(),
                monthlyExpense.getTotalAmount(),
                monthlyExpense.getPreviousTotalAmount(),
                monthlyExpense.getChangePercentage(),
                monthlyExpense.getUserId(),
                category,
                transaction
        );
    }
}
