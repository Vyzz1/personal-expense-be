package com.huynh.personal_expense_be.modules.category.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CategoryAnalysisResponse(
        int month,
        int year,
        BigDecimal totalAmount,
        int transactionCount,
        UUID id,
        String name
){

}
