package com.huynh.personal_expense_be.modules.expense.application.port.in;

import com.huynh.personal_expense_be.modules.expense.application.dto.UpdateExpenseCommand;

public interface UpdateMonthlyExpenseUseCase {

    void updateMonthlyExpense(UpdateExpenseCommand command);
}
