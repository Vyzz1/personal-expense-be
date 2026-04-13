package com.huynh.personal_expense_be.modules.transaction.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record GetTransactionCommand(
        int page,
        int size,
        String sortBy,
        String sortOrder,
        String userId,
        String description,
        List<UUID> categoryIds,
        List<String> type,
        String fromDate,
        String toDate,
        int month,
        int year,
        BigDecimal minAmount,
        BigDecimal maxAmount
) {
}
