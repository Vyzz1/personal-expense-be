package com.huynh.personal_expense_be.modules.expense.presentation.request;

public record GetMonthlyExpenseRequest(
        int month,
        int year
) {
}
