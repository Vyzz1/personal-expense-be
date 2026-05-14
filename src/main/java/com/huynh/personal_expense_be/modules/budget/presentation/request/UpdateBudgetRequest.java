package com.huynh.personal_expense_be.modules.budget.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateBudgetRequest(
        @NotBlank String name,
        @Positive @Positive BigDecimal limitAmount
) {
}
