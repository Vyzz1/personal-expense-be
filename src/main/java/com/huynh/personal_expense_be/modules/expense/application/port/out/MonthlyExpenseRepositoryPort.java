package com.huynh.personal_expense_be.modules.expense.application.port.out;

import com.huynh.personal_expense_be.modules.expense.domain.MonthlyExpense;

public interface MonthlyExpenseRepositoryPort {

    MonthlyExpense saveMonthlyExpense(MonthlyExpense monthlyExpense);

    MonthlyExpense findByUserIdAndMonth(String userId, int month, int year);

}