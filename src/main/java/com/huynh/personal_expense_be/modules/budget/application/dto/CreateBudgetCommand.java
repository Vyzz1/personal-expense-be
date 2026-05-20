package com.huynh.personal_expense_be.modules.budget.application.dto;

import com.huynh.personal_expense_be.modules.budget.domain.Budget;
import com.huynh.personal_expense_be.modules.budget.domain.BudgetStatus;
import com.huynh.personal_expense_be.modules.category.domain.Category;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

public record CreateBudgetCommand (
    String name,
    String userId,
    UUID categoryId,
    BigDecimal limitAmount,
    float thresholdPercentage
){

    public static Budget to(CreateBudgetCommand command, Category category) {
        return Budget.builder()
                .name(command.name)
                .userId(command.userId)
                .category(category)
                .limitAmount(command.limitAmount)
                .thresholdPercentage(command.thresholdPercentage)
                .spentAmount(BigDecimal.ZERO)
                .status(BudgetStatus.ACTIVE)
                .period(YearMonth.now())
                .build();
    }
}
