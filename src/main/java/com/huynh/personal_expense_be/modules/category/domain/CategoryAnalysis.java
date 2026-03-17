package com.huynh.personal_expense_be.modules.category.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class CategoryAnalysis {

    private UUID id;

    private int month;

    private int year;

    private String name;

    private BigDecimal totalAmount;

    private Long transactionCount;
}
