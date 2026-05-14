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


    public Budget update(String name, BigDecimal limitAmount) {
        return this.toBuilder()
                .name(name)
                .limitAmount(limitAmount)
                .updatedAt(Instant.now())
                .build();
    }

    public Budget addSpentAmount(BigDecimal amount) {
        return this.toBuilder()
                .spentAmount(this.spentAmount.add(amount))
                .updatedAt(Instant.now())
                .build();
    }

    public Budget subtractSpentAmount(BigDecimal amount) {
        return this.toBuilder()
                .spentAmount(this.spentAmount.subtract(amount))
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


}
