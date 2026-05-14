package com.huynh.personal_expense_be.modules.budget.application.port.out;

import java.math.BigDecimal;
import java.util.UUID;

public interface BudgetPersistencePort {
    void ensureBudgetExists(String userId, UUID categoryId, String period);
    int incrementSpentAmount(String userId, UUID categoryId, String period, BigDecimal amount);
    int expireBudgetsBeforePeriod(String period);
}
