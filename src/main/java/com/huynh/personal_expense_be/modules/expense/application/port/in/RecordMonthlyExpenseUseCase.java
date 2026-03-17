package com.huynh.personal_expense_be.modules.expense.application.port.in;

import com.huynh.personal_expense_be.modules.expense.application.dto.RecordExpenseCommand;

import java.util.List;

public interface RecordMonthlyExpenseUseCase {

    void recordMonthlyExpense(RecordExpenseCommand command);
    void recordMonthlyExpenses(List<RecordExpenseCommand> commands);
}
