package com.huynh.personal_expense_be.modules.budget.application.service;

import com.huynh.personal_expense_be.modules.budget.application.dto.BudgetResponse;
import com.huynh.personal_expense_be.modules.budget.application.dto.CreateBudgetCommand;
import com.huynh.personal_expense_be.modules.budget.application.dto.UpdateBudgetCommand;
import com.huynh.personal_expense_be.modules.budget.application.port.out.BudgetRepositoryPort;
import com.huynh.personal_expense_be.modules.budget.domain.Budget;
import com.huynh.personal_expense_be.modules.budget.domain.BudgetStatus;
import com.huynh.personal_expense_be.modules.category.application.port.out.CategoryRepositoryPort;
import com.huynh.personal_expense_be.modules.category.domain.Category;
import com.huynh.personal_expense_be.shared.exception.BusinessValidationException;
import com.huynh.personal_expense_be.shared.exception.DuplicateException;
import com.huynh.personal_expense_be.shared.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommandBudgetServiceTest {

    @Mock
    private BudgetRepositoryPort budgetRepositoryPort;

    @Mock
    private CategoryRepositoryPort categoryRepositoryPort;

    @InjectMocks
    private CommandBudgetService commandBudgetService;

    @Test
    void createBudget_withCategory_success() {
        UUID categoryId = UUID.randomUUID();
        CreateBudgetCommand command = new CreateBudgetCommand("Food Budget", "user-1", categoryId, new BigDecimal("500.00"));
        Category category = Category.builder()
                .id(categoryId)
                .name("Food")
                .userId("user-1")
                .build();

        when(categoryRepositoryPort.findById(categoryId)).thenReturn(Optional.of(category));
        when(budgetRepositoryPort.existsByCategoryIdAndUserId(categoryId, "user-1")).thenReturn(false);

        Budget savedBudget = Budget.builder()
                .id(UUID.randomUUID())
                .name("Food Budget")
                .userId("user-1")
                .limitAmount(new BigDecimal("500.00"))
                .spentAmount(BigDecimal.ZERO)
                .category(category)
                .status(BudgetStatus.ACTIVE).period(YearMonth.now())
                .period(YearMonth.now())
                .build();
        
        when(budgetRepositoryPort.save(any(Budget.class))).thenReturn(savedBudget);

        BudgetResponse response = commandBudgetService.createBudget(command);

        assertNotNull(response);
        assertEquals("Food Budget", response.name());
        assertEquals(categoryId, response.category().id());
        verify(budgetRepositoryPort).save(any(Budget.class));
    }

    @Test
    void createBudget_categoryNotFound_throwsBusinessValidationException() {
        UUID categoryId = UUID.randomUUID();
        CreateBudgetCommand command = new CreateBudgetCommand("Food Budget", "user-1", categoryId, new BigDecimal("500.00"));

        when(categoryRepositoryPort.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(BusinessValidationException.class, () -> commandBudgetService.createBudget(command));
        verify(budgetRepositoryPort, never()).save(any());
    }

    @Test
    void createBudget_duplicateCategoryBudget_throwsBusinessValidationException() {
        UUID categoryId = UUID.randomUUID();
        CreateBudgetCommand command = new CreateBudgetCommand("Food Budget", "user-1", categoryId, new BigDecimal("500.00"));
        Category category = Category.builder().id(categoryId).name("Food").build();

        when(categoryRepositoryPort.findById(categoryId)).thenReturn(Optional.of(category));
        when(budgetRepositoryPort.existsByCategoryIdAndUserId(categoryId, "user-1")).thenReturn(true);

        assertThrows(BusinessValidationException.class, () -> commandBudgetService.createBudget(command));
        verify(budgetRepositoryPort, never()).save(any());
    }

    @Test
    void createBudget_duplicateOverallBudget_throwsBusinessValidationException() {
        CreateBudgetCommand command = new CreateBudgetCommand("Overall", "user-1", null, new BigDecimal("500.00"));

        when(budgetRepositoryPort.existsOverallByUserId("user-1")).thenReturn(true);

        assertThrows(DuplicateException.class, () -> commandBudgetService.createBudget(command));
        verify(budgetRepositoryPort, never()).save(any());
    }

    @Test
    void updateBudget_success() {
        UUID budgetId = UUID.randomUUID();
        UpdateBudgetCommand command = new UpdateBudgetCommand(budgetId, "user-1", "New Name", new BigDecimal("1000.00"), "ACTIVE");
        
        Budget existingBudget = Budget.builder()
                .id(budgetId)
                .name("Old Name")
                .userId("user-1")
                .limitAmount(new BigDecimal("500.00"))
                .status(BudgetStatus.ACTIVE).period(YearMonth.now())
                .build();

        Budget updatedBudget = existingBudget.toBuilder()
                .name("New Name")
                .limitAmount(new BigDecimal("1000.00"))
                .build();
                
        // using existingBudget.update which returns new Budget inside service

        when(budgetRepositoryPort.findById("user-1", budgetId)).thenReturn(Optional.of(existingBudget));
        when(budgetRepositoryPort.save(any(Budget.class))).thenReturn(updatedBudget);

        BudgetResponse response = commandBudgetService.updateBudget(command);

        assertNotNull(response);
        assertEquals("New Name", response.name());
        assertEquals(new BigDecimal("1000.00"), response.limitAmount());
        verify(budgetRepositoryPort).save(any(Budget.class));
    }

    @Test
    void updateBudget_notFound_throwsNotFoundException() {
        UUID budgetId = UUID.randomUUID();
        UpdateBudgetCommand command = new UpdateBudgetCommand(budgetId, "user-1", "New Name", new BigDecimal("1000.00"), "ACTIVE");

        when(budgetRepositoryPort.findById("user-1", budgetId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> commandBudgetService.updateBudget(command));
        verify(budgetRepositoryPort, never()).save(any());
    }

    @Test
    void deleteBudget_success() {
        UUID budgetId = UUID.randomUUID();
        
        commandBudgetService.deleteBudget(budgetId);

        verify(budgetRepositoryPort, times(1)).deleteById(budgetId);
    }
}
