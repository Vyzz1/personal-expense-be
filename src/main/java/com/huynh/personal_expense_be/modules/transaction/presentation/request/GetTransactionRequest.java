package com.huynh.personal_expense_be.modules.transaction.presentation.request;

import lombok.Data;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

@Data
public class GetTransactionRequest {
    private int page = 0;
    private int size = 10;
    private String sortBy = "occurredAt";
    private String sortOrder = "desc";
    private String description;
    private List<UUID> categoryIds;
    private List<String> type;
    private String fromDate;
    private String toDate;
    private int month;
    private int year;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
}
