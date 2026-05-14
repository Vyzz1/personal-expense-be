package com.huynh.personal_expense_be.modules.budget.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateBudgetRequest(
        @NotBlank String name,
        UUID categoryId,
        @NotNull @Positive BigDecimal limitAmount,
        @Positive float thresholdPercentage

) {
}
