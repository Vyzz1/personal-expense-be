package com.huynh.personal_expense_be.modules.category.application.dto;

import com.huynh.personal_expense_be.modules.category.domain.CategoryAnalysis;

import java.math.BigDecimal;
import java.util.UUID;

public record CategoryAnalysisResponse(
        int month,
        int year,
        BigDecimal totalAmount,
        Long transactionCount,
        UUID id,
        String name
){

    public static  CategoryAnalysisResponse from(CategoryAnalysis categoryAnalysis) {
        return new CategoryAnalysisResponse(
                categoryAnalysis.getMonth(),
                categoryAnalysis.getYear(),
                categoryAnalysis.getTotalAmount(),
                categoryAnalysis.getTransactionCount(),
                categoryAnalysis.getId(),
                categoryAnalysis.getName()
        );
    }

}
