package com.huynh.personal_expense_be.modules.expense.domain;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
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

    private UUID transactionId;

    private UUID categoryId;

    private Instant createdAt;

    private Instant updatedAt;

    private Instant isDeleted;
}
