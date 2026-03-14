package com.huynh.personal_expense_be.modules.transaction.application.dto;

public record ImportTransactionCommand(
        String userId,
        String filePath
) {
}
