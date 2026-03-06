package com.huynh.personal_expense_be.modules.expense.domain;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Getter @Builder(toBuilder = true)
public class MonthlyExpense {

    private UUID id;

    private String userId;

    private int month;

    private int year;

    private BigDecimal totalAmount;

    private BigDecimal previousTotalAmount;

    private BigDecimal changePercentage;

    private Instant createdAt;

    private Instant updatedAt;

    private Instant isDeleted;

    public void updateTotalAmount(BigDecimal additionalAmount) {
        BigDecimal previousTotal = this.totalAmount;
        BigDecimal newTotal = previousTotal.add(additionalAmount);

        this.totalAmount = newTotal;
        this.changePercentage = previousTotal.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : newTotal.subtract(previousTotal).divide(previousTotal, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    public void withPrevious(MonthlyExpense previousExpense) {

        if (previousExpense == null) return;

        this.previousTotalAmount = previousExpense.getTotalAmount();

        if (previousExpense.getTotalAmount().compareTo(BigDecimal.ZERO) == 0) {
            this.changePercentage = BigDecimal.ZERO;
        } else {

            this.changePercentage =
                    this.totalAmount.subtract(previousExpense.getTotalAmount())
                            .divide(previousExpense.getTotalAmount(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
        }
    }

}
