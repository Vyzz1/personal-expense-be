package com.huynh.personal_expense_be.modules.budget.application.dto;


import java.math.BigDecimal;
import java.util.UUID;

public record UpdateBudgetCommand(
        UUID id,
        String userId,
        String name,
        BigDecimal limitAmount,
        float thresholdPercentage
) {
}
