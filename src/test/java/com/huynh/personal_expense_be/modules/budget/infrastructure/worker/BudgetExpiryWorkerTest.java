package com.huynh.personal_expense_be.modules.budget.infrastructure.worker;

import com.huynh.personal_expense_be.modules.budget.application.port.out.BudgetPersistencePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetExpiryWorkerTest {

    @Mock
    private BudgetPersistencePort budgetPersistencePort;

    @InjectMocks
    private BudgetExpiryWorker budgetExpiryWorker;

    @Test
    void expireOldBudgets_shouldCallExpireUsingCurrentPeriod() {
        when(budgetPersistencePort.expireBudgetsBeforePeriod(anyString())).thenReturn(0);

        budgetExpiryWorker.expireOldBudgets();

        verify(budgetPersistencePort).expireBudgetsBeforePeriod(anyString());
    }
}
