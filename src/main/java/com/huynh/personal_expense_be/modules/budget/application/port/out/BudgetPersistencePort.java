package com.huynh.personal_expense_be.modules.budget.application.port.out;

import java.math.BigDecimal;

public interface BudgetPersistencePort {
    void ensureBudgetExists(String userId, String categoryId, String period);
    void incrementSpentAmount(String userId, String categoryId, String period, BigDecimal amount);
}
