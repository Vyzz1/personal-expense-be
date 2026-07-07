package com.huynh.personal_expense_be.modules.budget.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Request body for creating a budget")
public record CreateBudgetRequest(
        @Schema(description = "Budget name", example = "Monthly Food Budget")
        @NotBlank String name,

        @Schema(description = "Category to track. Omit for an overall budget covering all categories.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID categoryId,

        @Schema(description = "Spending limit amount", example = "3000000")
        @NotNull @Positive BigDecimal limitAmount,

        @Schema(description = "Alert threshold as a percentage of limitAmount (50–100). An alert is sent when spending crosses this threshold.", example = "80")
        @DecimalMin(value = "50.0") @DecimalMax(value = "100.0") float thresholdPercentage
) {
}
