package com.huynh.personal_expense_be.modules.expense.application.port.out;

import com.huynh.personal_expense_be.modules.expense.domain.MonthlyExpense;

import java.util.List;

public interface MonthlyExpenseRepositoryPort {

    MonthlyExpense saveMonthlyExpense(MonthlyExpense monthlyExpense);

    List<MonthlyExpense> saveAllMonthlyExpenses(List<MonthlyExpense> monthlyExpenses);

    MonthlyExpense findByUserIdAndMonth(String userId, int month, int year);

    /**
     * Bulk-fetch MonthlyExpense records matching any of the given (month, year) pairs for a single userId.
     * keys: list of int[]{month, year}
     */
    List<MonthlyExpense> findAllByUserIdAndMonthYearPairs(String userId, List<int[]> monthYearPairs);

    List<MonthlyExpense> findThreeMonthCompare(String userId, int month, int year);

}