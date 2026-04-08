package com.huynh.personal_expense_be.modules.expense.domain;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor @AllArgsConstructor
@SuperBuilder
public class MonthlyExpenseAnalysis extends MonthlyExpense {

    private BigDecimal previousTotalAmount;
    private BigDecimal changePercentage;

    
}
