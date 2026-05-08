package com.huynh.personal_expense_be.modules.budget.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateBudgetCommand (
    String name,
    String userId,
    UUID categoryId,
    BigDecimal limitAmount
){
}
