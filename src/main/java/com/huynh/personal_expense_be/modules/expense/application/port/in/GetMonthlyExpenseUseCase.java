package com.huynh.personal_expense_be.modules.expense.application.port.in;

import com.huynh.personal_expense_be.modules.expense.application.dto.GetMonthlyExpenseCommand;
import com.huynh.personal_expense_be.modules.expense.application.dto.GetMonthlyExpenseResponse;

public interface GetMonthlyExpenseUseCase {

    GetMonthlyExpenseResponse getMonthlyExpense(GetMonthlyExpenseCommand command);
}
