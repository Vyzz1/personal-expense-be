package com.huynh.personal_expense_be.modules.transaction.application.dto;

import java.math.BigDecimal;

public record TransactionCsv(
                BigDecimal amount,
                String description,
                String date,
                String type,
                String category,
                String userId) {
}
