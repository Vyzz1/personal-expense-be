package com.huynh.personal_expense_be.modules.budget.application.service;

import com.huynh.personal_expense_be.modules.budget.application.dto.BudgetResponse;
import com.huynh.personal_expense_be.modules.budget.application.port.out.BudgetRepositoryPort;
import com.huynh.personal_expense_be.modules.budget.domain.Budget;
import com.huynh.personal_expense_be.modules.budget.domain.BudgetStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QueryBudgetServiceTest {

    @Mock
    private BudgetRepositoryPort budgetRepositoryPort;

    @InjectMocks
    private QueryBudgetService queryBudgetService;

    @Test
    void getBudgetsByUserId_success() {
        Budget budget = Budget.builder()
                .id(UUID.randomUUID())
                .userId("user-1")
                .limitAmount(new BigDecimal("100"))
                .spentAmount(BigDecimal.ZERO)
                .status(BudgetStatus.ACTIVE)
                .period(YearMonth.now())
                .build();
                
        when(budgetRepositoryPort.findByUserId("user-1")).thenReturn(List.of(budget));

        List<BudgetResponse> responses = queryBudgetService.getBudgetsByUserId("user-1");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(budget.getId(), responses.get(0).id());
    }

    @Test
    void getBudgetById_found_returnsBudgetResponse() {
        UUID budgetId = UUID.randomUUID();
        Budget budget = Budget.builder()
                .id(budgetId)
                .userId("user-1")
                .limitAmount(new BigDecimal("100"))
                .spentAmount(BigDecimal.ZERO)
                .status(BudgetStatus.ACTIVE)
                .period(YearMonth.now())
                .build();
                
        when(budgetRepositoryPort.findById("user-1", budgetId)).thenReturn(Optional.of(budget));

        BudgetResponse response = queryBudgetService.getBudgetById("user-1", budgetId);

        assertNotNull(response);
        assertEquals(budgetId, response.id());
    }

    @Test
    void getBudgetById_notFound_returnsNull() {
        UUID budgetId = UUID.randomUUID();
        when(budgetRepositoryPort.findById("user-1", budgetId)).thenReturn(Optional.empty());

        BudgetResponse response = queryBudgetService.getBudgetById("user-1", budgetId);

        assertNull(response);
    }

    @Test
    void getBudgetsByPeriod_success() {
        String period = "2026-05";
        Budget budget = Budget.builder()
                .id(UUID.randomUUID())
                .userId("user-1")
                .limitAmount(new BigDecimal("100"))
                .spentAmount(BigDecimal.ZERO)
                .status(BudgetStatus.ACTIVE)
                .period(YearMonth.parse(period))
                .build();
                
        when(budgetRepositoryPort.findByUserIdAndPeriod("user-1", period)).thenReturn(List.of(budget));

        List<BudgetResponse> responses = queryBudgetService.getBudgetsByPeriod("user-1", period);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(budget.getId(), responses.get(0).id());
    }
}
