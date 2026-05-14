package com.huynh.personal_expense_be.modules.budget.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.huynh.personal_expense_be.modules.budget.domain.Budget;
import com.huynh.personal_expense_be.modules.category.application.dto.CategoryResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BudgetResponse(
        UUID id,
        String name,
        BigDecimal limitAmount,
        BigDecimal spentAmount,
        CategoryResponse category,
        String status,
        String period,
        Instant createdAt,
        Instant updatedAt,
        float thresholdPercentage
) {

    public static BudgetResponse from(Budget budget) {
        return new BudgetResponse(
                budget.getId(),
                budget.getName(),
                budget.getLimitAmount(),
                budget.getSpentAmount(),
                budget.getCategory() != null ? CategoryResponse.from(budget.getCategory()) : null,
                budget.getStatus().toString(),
                budget.getPeriod().toString(),
                budget.getCreatedAt(),
                budget.getUpdatedAt(),
                budget.getThresholdPercentage()
        );
    }
}
