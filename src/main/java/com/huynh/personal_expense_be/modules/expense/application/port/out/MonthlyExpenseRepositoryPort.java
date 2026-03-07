package com.huynh.personal_expense_be.modules.expense.application.port.out;

import com.huynh.personal_expense_be.modules.expense.domain.MonthlyExpense;

import java.util.List;

public interface MonthlyExpenseRepositoryPort {

    MonthlyExpense saveMonthlyExpense(MonthlyExpense monthlyExpense);

    MonthlyExpense findByUserIdAndMonth(String userId, int month, int year);

    List<MonthlyExpense> findThreeMonthCompare(String userId, int month, int year);

}