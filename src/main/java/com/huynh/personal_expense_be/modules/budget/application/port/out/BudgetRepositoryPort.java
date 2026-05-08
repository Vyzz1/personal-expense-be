package com.huynh.personal_expense_be.modules.budget.application.port.out;

import com.huynh.personal_expense_be.modules.budget.domain.Budget;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepositoryPort {

    Budget save(Budget budget);

    Optional<Budget> findById(String userId, UUID budgetId);

    void  deleteById( UUID budgetId);

    List<Budget> findByUserIdAndPeriod(String userId, String period);

    boolean existsByUserId(String userId);

    List<Budget> findByUserId(String userId);

    boolean existsOverallByUserId(String userId);

    boolean existsByCategoryIdAndUserId(UUID categoryId, String userId);
}
