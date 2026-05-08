package com.huynh.personal_expense_be.modules.budget.application.port.in;

import com.huynh.personal_expense_be.modules.budget.application.dto.BudgetResponse;
import com.huynh.personal_expense_be.modules.budget.application.dto.UpdateBudgetCommand;

public interface UpdateBudgetUseCase {

    BudgetResponse updateBudget(UpdateBudgetCommand command);
}
