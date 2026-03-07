package com.huynh.personal_expense_be.modules.expense.application.service;

import com.huynh.personal_expense_be.modules.expense.application.dto.GetMonthlyExpenseCommand;
import com.huynh.personal_expense_be.modules.expense.application.dto.GetMonthlyExpenseResponse;
import com.huynh.personal_expense_be.modules.expense.application.dto.GetThreeMonthCompareCommand;
import com.huynh.personal_expense_be.modules.expense.application.port.in.GetMonthlyExpenseUseCase;
import com.huynh.personal_expense_be.modules.expense.application.port.in.GetThreeMonthCompareUseCase;
import com.huynh.personal_expense_be.modules.expense.application.port.out.MonthlyExpenseRepositoryPort;
import com.huynh.personal_expense_be.modules.expense.domain.MonthlyExpense;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueryMonthExpenseService implements GetMonthlyExpenseUseCase, GetThreeMonthCompareUseCase {

    private final MonthlyExpenseRepositoryPort monthlyExpenseRepositoryPort;

    @Override
    public GetMonthlyExpenseResponse getMonthlyExpense(GetMonthlyExpenseCommand command) {
        int month = command.month();
        int year = command.year();

        MonthlyExpense monthlyExpense = monthlyExpenseRepositoryPort.findByUserIdAndMonth(command.userId(), month, year);

        if (monthlyExpense == null) {
            log.info("No MonthlyExpense found for userId: {}, month: {}, year: {}. Returning default response.", command.userId(), month, year);
            return new GetMonthlyExpenseResponse(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    null,
                    month,
                    year
            );
        }

        log.info("MonthlyExpense found for userId: {}, month: {}, year: {}. Returning response.", command.userId(), month, year);

        return GetMonthlyExpenseResponse.of(monthlyExpense);
    }

    @Override
    public List<GetMonthlyExpenseResponse> getThreeMonthCompare(GetThreeMonthCompareCommand command) {

        int month = command.month();
        int year = command.year();

        List<MonthlyExpense> expenses = monthlyExpenseRepositoryPort.findThreeMonthCompare(command.userId(), month, year);

        return expenses.stream()
                .map(GetMonthlyExpenseResponse::of)
                .toList();
    }
}
