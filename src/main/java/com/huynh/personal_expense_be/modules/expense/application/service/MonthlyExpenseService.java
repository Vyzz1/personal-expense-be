package com.huynh.personal_expense_be.modules.expense.application.service;

import com.huynh.personal_expense_be.modules.expense.application.dto.MonthlyExpenseResponse;
import com.huynh.personal_expense_be.modules.expense.application.dto.RecordExpenseCommand;
import com.huynh.personal_expense_be.modules.expense.application.port.in.RecordMonthlyExpenseUseCase;
import com.huynh.personal_expense_be.modules.expense.application.port.out.MonthlyExpenseRepositoryPort;
import com.huynh.personal_expense_be.modules.expense.domain.MonthlyExpense;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonthlyExpenseService implements RecordMonthlyExpenseUseCase {

    private final MonthlyExpenseRepositoryPort monthlyExpenseRepositoryPort;

    @Override
    public MonthlyExpenseResponse recordMonthlyExpense(RecordExpenseCommand command) {

        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime now = command.occurredAt().atZone(zoneId);

        int month = now.getMonthValue();
        int year = now.getYear();

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

        return MonthlyExpenseResponse.from(saved, null, null);
    }
}
