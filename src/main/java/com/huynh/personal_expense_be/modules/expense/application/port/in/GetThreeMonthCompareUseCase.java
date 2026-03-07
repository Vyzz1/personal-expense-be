package com.huynh.personal_expense_be.modules.expense.application.port.in;

import com.huynh.personal_expense_be.modules.expense.application.dto.GetMonthlyExpenseResponse;
import com.huynh.personal_expense_be.modules.expense.application.dto.GetThreeMonthCompareCommand;

import java.util.List;

public interface GetThreeMonthCompareUseCase {

        List<GetMonthlyExpenseResponse> getThreeMonthCompare(GetThreeMonthCompareCommand command);
}
