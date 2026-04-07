package com.huynh.personal_expense_be.modules.category.application.dto;

public record GetCategoryAnalysisCommand (
        String userId,
        Integer month,
        Integer year
){
}
