package com.huynh.personal_expense_be.modules.expense.application.port.in;

import com.huynh.personal_expense_be.modules.expense.application.dto.MonthlyExpenseResponse;
import com.huynh.personal_expense_be.modules.expense.application.dto.RecordExpenseCommand;

public interface RecordMonthlyExpenseUseCase {

    MonthlyExpenseResponse recordMonthlyExpense(RecordExpenseCommand command);
}
