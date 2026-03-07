package com.huynh.personal_expense_be.modules.expense.application.dto;

public record GetMonthlyExpenseCommand(
        String userId,
        int month,
        int year
) {
}
