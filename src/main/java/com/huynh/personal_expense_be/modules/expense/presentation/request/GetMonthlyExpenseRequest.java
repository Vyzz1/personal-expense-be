package com.huynh.personal_expense_be.modules.expense.presentation.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GetMonthlyExpenseRequest(
        @NotNull @Min(1) @Max(12) Integer month,
        @NotNull @Min(2000) @Max(2100) Integer year
) {
}
