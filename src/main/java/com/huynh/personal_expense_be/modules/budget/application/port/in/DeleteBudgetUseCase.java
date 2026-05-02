package com.huynh.personal_expense_be.modules.budget.application.port.in;

import java.util.UUID;

public interface DeleteBudgetUseCase {

    void deleteBudget(UUID budgetId);
}
