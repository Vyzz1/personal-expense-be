package com.huynh.personal_expense_be.modules.expense.domain;

import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@SuperBuilder(toBuilder = true)
@Slf4j
@Getter @Builder(toBuilder = true) @MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyExpense {

    private UUID id;

    private String userId;

    private int month;

    private int year;

    private BigDecimal totalAmount;

    private Instant createdAt;

    private Instant updatedAt;

    private Instant isDeleted;


}
