package com.huynh.personal_expense_be.modules.budget.application.service;

import com.huynh.personal_expense_be.modules.budget.application.dto.BudgetResponse;
import com.huynh.personal_expense_be.modules.budget.application.dto.CreateBudgetCommand;
import com.huynh.personal_expense_be.modules.budget.application.dto.UpdateBudgetCommand;
import com.huynh.personal_expense_be.modules.budget.application.port.in.CreateBudgetUseCase;
import com.huynh.personal_expense_be.modules.budget.application.port.in.DeleteBudgetUseCase;
import com.huynh.personal_expense_be.modules.budget.application.port.in.UpdateBudgetUseCase;
import com.huynh.personal_expense_be.modules.budget.application.port.out.BudgetRepositoryPort;
import com.huynh.personal_expense_be.modules.budget.domain.Budget;
import com.huynh.personal_expense_be.modules.category.application.port.out.CategoryRepositoryPort;

import com.huynh.personal_expense_be.modules.category.domain.Category;
import com.huynh.personal_expense_be.shared.exception.BusinessValidationException;

import com.huynh.personal_expense_be.shared.exception.DuplicateException;
import com.huynh.personal_expense_be.shared.exception.NotFoundException;
import com.huynh.personal_expense_be.shared.exception.ValidationFieldError;
import lombok.RequiredArgsConstructor;


import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommandBudgetService implements CreateBudgetUseCase, DeleteBudgetUseCase, UpdateBudgetUseCase {

    private final BudgetRepositoryPort budgetRepositoryPort;
    private final CategoryRepositoryPort categoryRepositoryPort;

    @Override
    public BudgetResponse createBudget(CreateBudgetCommand command) {

        Category category = null;

        if (command.categoryId() != null) {
            category = categoryRepositoryPort.findById(command.categoryId()).orElseThrow(
                    () -> new BusinessValidationException(List.of(ValidationFieldError.of(
                            "categoryId",
                            command.categoryId().toString(),
                            "Category not found",
                            null
                    )))
            );


            if (budgetRepositoryPort.existsByCategoryIdAndUserId(command.categoryId(), command.userId())) {

                throw new BusinessValidationException(List.of(ValidationFieldError.of(
                        "categoryId",
                        command.categoryId().toString(),
                        "Budget for category '" + category.getName() + "' already exists for user '" + command.userId() + "'",
                        null
                )));
            }

        } else {
            if (budgetRepositoryPort.existsOverallByUserId(command.userId())) {
                throw new DuplicateException("Overall budget for user '" + command.userId() + "' already exists");
            }
        }


        Budget budget = CreateBudgetCommand.to(command, category);

        return BudgetResponse.from(budgetRepositoryPort.save(budget));
    }

    @Override
    public void deleteBudget(String userId, UUID budgetId) {
        budgetRepositoryPort.findById(userId, budgetId)
                .orElseThrow(() -> new NotFoundException("Budget with id '" + budgetId + "' not found for user '" + userId + "'"));
        budgetRepositoryPort.deleteById(budgetId);
    }

    @Override
    public BudgetResponse updateBudget(UpdateBudgetCommand command) {
        Budget budget = budgetRepositoryPort.findById(command.userId(), command.id())
                .orElseThrow(() -> new NotFoundException("Budget with id '" + command.id() + "' not found for user '" + command.userId() + "'"
                ));

        Budget updatedBudget = budget.update(command.name(), command.limitAmount(), command.thresholdPercentage());

        return BudgetResponse.from(budgetRepositoryPort.save(updatedBudget));
    }
}
