package com.huynh.personal_expense_be.modules.budget.application.port.in;

import com.huynh.personal_expense_be.modules.budget.application.dto.BudgetResponse;

import java.util.List;
import java.util.UUID;

public interface GetBudgetUseCase {

    List<BudgetResponse> getBudgetsByUserId(String userId);

    BudgetResponse getBudgetById(String userId, UUID budgetId);

    List<BudgetResponse> getBudgetsByPeriod(String userId, String period);
}
