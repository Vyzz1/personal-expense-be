package com.huynh.personal_expense_be.modules.transaction.domain;

import com.huynh.personal_expense_be.modules.category.domain.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter @NoArgsConstructor @AllArgsConstructor @Builder(toBuilder = true)
public class Transaction {

    private UUID id;

    private String userId;

    private Category category;

    private String description;

    private BigDecimal amount;

    private TransactionType type;

    private Instant occurredAt;

    private Instant createdAt;

    private Instant updatedAt;

    private Instant isDeleted;
}
