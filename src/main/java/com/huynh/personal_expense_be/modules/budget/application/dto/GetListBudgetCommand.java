package com.huynh.personal_expense_be.modules.budget.application.dto;

import com.huynh.personal_expense_be.modules.budget.domain.BudgetStatus;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

public record GetListBudgetCommand(
        int page,
        int size,
        String sortBy,
        String sortOrder,
        String userId,
        BudgetStatus status,
        String search,
        List<UUID> categoryIds,
        YearMonth period
) {
}
