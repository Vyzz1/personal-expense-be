package com.huynh.personal_expense_be.modules.expense.application.service;

import com.huynh.personal_expense_be.modules.expense.application.dto.*;
import com.huynh.personal_expense_be.modules.expense.application.port.in.DeductMonthlyExpenseUseCase;
import com.huynh.personal_expense_be.modules.expense.application.port.in.RecordMonthlyExpenseUseCase;
import com.huynh.personal_expense_be.modules.expense.application.port.in.UpdateMonthlyExpenseUseCase;
import com.huynh.personal_expense_be.modules.expense.application.port.out.MonthlyExpenseRepositoryPort;
import com.huynh.personal_expense_be.modules.expense.domain.MonthlyExpense;
import com.huynh.personal_expense_be.shared.utility.Utility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;



@Slf4j
@Service
@RequiredArgsConstructor
public class CommandMonthlyExpenseService implements RecordMonthlyExpenseUseCase, DeductMonthlyExpenseUseCase, UpdateMonthlyExpenseUseCase {

    private final MonthlyExpenseRepositoryPort monthlyExpenseRepositoryPort;

    @Override
    public void recordMonthlyExpense(RecordExpenseCommand command) {



        int month = Utility.getMonthFromInstant(command.occurredAt());
        int year =  Utility.getYearFromInstant(command.occurredAt());

        MonthlyExpense monthlyExpense = monthlyExpenseRepositoryPort.findByUserIdAndMonth(command.userId(), month, year);

        if (monthlyExpense == null) {

            MonthlyExpense previousMonthExpense = monthlyExpenseRepositoryPort.findByUserIdAndMonth(command.userId(), month == 1 ? 12 : month - 1, month == 1 ? year - 1 : year);

            monthlyExpense = MonthlyExpense.builder()
                    .userId(command.userId())
                    .month(month)
                    .year(year)
                    .totalAmount(command.amount())
                    .build();

            monthlyExpense.withPrevious(previousMonthExpense);

        } else {
            log.info("Existing MonthlyExpense found for userId: {}, month: {}, year: {}. Accumulating total amount.", command.userId(), month, year);
            monthlyExpense.updateTotalAmount(command.amount());
            monthlyExpense = monthlyExpense.toBuilder()
                    .totalAmount(monthlyExpense.getTotalAmount())
                    .changePercentage(monthlyExpense.getChangePercentage())
                    .build();
        }

        MonthlyExpense saved =  monthlyExpenseRepositoryPort.saveMonthlyExpense(monthlyExpense);

        MonthlyExpenseResponse.from(saved, null, null);
    }

    @Override
    public void deductExpense(DeductExpenseCommand command) {
        int month = Utility.getMonthFromInstant(command.occurredAt());
        int year =  Utility.getYearFromInstant(command.occurredAt());

        MonthlyExpense monthlyExpense = monthlyExpenseRepositoryPort.findByUserIdAndMonth(command.userId(), month, year);

        if (monthlyExpense == null) {
            log.warn("No MonthlyExpense found for userId: {}, month: {}, year: {}. Cannot deduct expense.", command.userId(), month, year);
            return;
        }

        log.info("Existing MonthlyExpense found for userId: {}, month: {}, year: {}. Deducting amount.", command.userId(), month, year);

        monthlyExpense.deductAmount(command.deductAmount());

        monthlyExpense = monthlyExpense.toBuilder()
                .totalAmount(monthlyExpense.getTotalAmount())
                .changePercentage(monthlyExpense.getChangePercentage())
                .build();

        monthlyExpenseRepositoryPort.saveMonthlyExpense(monthlyExpense);

    }

    @Override
    public void updateMonthlyExpense(UpdateExpenseCommand command) {
        int month = Utility.getMonthFromInstant(command.occurredAt());
        int year =  Utility.getYearFromInstant(command.occurredAt());

        MonthlyExpense monthlyExpense = monthlyExpenseRepositoryPort.findByUserIdAndMonth(command.userId(), month, year);

        if (monthlyExpense == null) {
            log.warn("No MonthlyExpense found for userId: {}, month: {}, year: {}. Cannot update expense.", command.userId(), month, year);
            return;
        }

        log.info("Existing MonthlyExpense found for userId: {}, month: {}, year: {}. Updating total amount.", command.userId(), month, year);

        monthlyExpense.updateTotalAmount(command.newAmount().subtract(command.oldAmount()));

        monthlyExpense = monthlyExpense.toBuilder()
                .totalAmount(monthlyExpense.getTotalAmount())
                .changePercentage(monthlyExpense.getChangePercentage())
                .build();

        monthlyExpenseRepositoryPort.saveMonthlyExpense(monthlyExpense);
    }


}
