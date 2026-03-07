package com.huynh.personal_expense_be.modules.expense.application.port.in;

import com.huynh.personal_expense_be.modules.expense.application.dto.DeductExpenseCommand;

public interface DeductMonthlyExpenseUseCase {

    void deductExpense(DeductExpenseCommand command);
}
