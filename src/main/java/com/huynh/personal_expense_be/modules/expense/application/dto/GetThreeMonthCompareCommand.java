package com.huynh.personal_expense_be.modules.expense.application.dto;

public record GetThreeMonthCompareCommand(
        String userId,
        int month,
        int year
) {
}
