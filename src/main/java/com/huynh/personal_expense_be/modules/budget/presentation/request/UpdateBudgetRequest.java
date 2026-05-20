package com.huynh.personal_expense_be.modules.budget.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateBudgetRequest(
        @NotBlank String name,
        @Positive BigDecimal limitAmount,
        @DecimalMin(value = "50.0") @DecimalMax(value = "100.0") float thresholdPercentage
) {
}
