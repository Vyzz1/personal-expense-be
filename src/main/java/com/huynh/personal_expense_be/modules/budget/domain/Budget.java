package com.huynh.personal_expense_be.modules.budget.domain;

import com.huynh.personal_expense_be.modules.category.domain.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Budget {

    private UUID id;

    private String name;

    private String userId;

    private BigDecimal limitAmount;

    private BigDecimal spentAmount;

    private float thresholdPercentage;

    private Category category;

    private BudgetStatus status;

    private YearMonth period;

    private Instant createdAt;

    private Instant updatedAt;

    private Instant isDeleted;


    public Budget update(String name, BigDecimal limitAmount, float thresholdPercentage) {
        BudgetStatus recalculatedStatus = recalculateStatus(limitAmount, thresholdPercentage);

        return this.toBuilder()
                .name(name)
                .limitAmount(limitAmount)
                .thresholdPercentage(thresholdPercentage)
                .status(recalculatedStatus)
                .updatedAt(Instant.now())
                .build();
    }

    private BudgetStatus recalculateStatus(BigDecimal newLimitAmount, float newThresholdPercentage) {
        if (this.status == BudgetStatus.EXPIRED) {
            return BudgetStatus.EXPIRED;
        }

        BigDecimal currentSpentAmount = this.spentAmount == null ? BigDecimal.ZERO : this.spentAmount;
        BigDecimal normalizedThreshold = newThresholdPercentage > 1
                ? BigDecimal.valueOf(newThresholdPercentage).divide(BigDecimal.valueOf(100))
                : BigDecimal.valueOf(newThresholdPercentage);
        BigDecimal exceededAmount = newLimitAmount.multiply(normalizedThreshold);

        return currentSpentAmount.compareTo(exceededAmount) >= 0 ? BudgetStatus.EXCEEDED : BudgetStatus.ACTIVE;
    }

    public Budget addSpentAmount(BigDecimal amount) {
        BigDecimal current = this.spentAmount != null ? this.spentAmount : BigDecimal.ZERO;
        return this.toBuilder()
                .spentAmount(current.add(amount))
                .updatedAt(Instant.now())
                .build();
    }

    public Budget subtractSpentAmount(BigDecimal amount) {
        BigDecimal current = this.spentAmount != null ? this.spentAmount : BigDecimal.ZERO;
        return this.toBuilder()
                .spentAmount(current.subtract(amount))
                .updatedAt(Instant.now())
                .build();
    }

    public Budget markAsExpired() {
        return this.toBuilder()
                .status(BudgetStatus.EXPIRED)
                .updatedAt(Instant.now())
                .build();
    }

    public Budget markAsExceeded() {
        return this.toBuilder()
                .status(BudgetStatus.EXCEEDED)
                .updatedAt(Instant.now())
                .build();
    }

    public YearMonth getCurrentPeriod() {
        return YearMonth.now();
    }


}
