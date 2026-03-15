package com.huynh.personal_expense_be.modules.transaction.application.dto;

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
        String type,
        String fromDate,
        String toDate,
        int month,
        int year
) {
}
