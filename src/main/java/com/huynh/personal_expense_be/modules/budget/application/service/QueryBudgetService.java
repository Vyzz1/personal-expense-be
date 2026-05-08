package com.huynh.personal_expense_be.modules.budget.application.service;

import com.huynh.personal_expense_be.modules.budget.application.dto.BudgetResponse;
import com.huynh.personal_expense_be.modules.budget.application.port.in.GetBudgetUseCase;
import com.huynh.personal_expense_be.modules.budget.application.port.out.BudgetRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QueryBudgetService implements GetBudgetUseCase {

    private  final BudgetRepositoryPort budgetRepositoryPort;

    @Override
    public List<BudgetResponse> getBudgetsByUserId(String userId) {
        return budgetRepositoryPort.findByUserId(userId).stream()
                .map(BudgetResponse::from)
                .toList();
    }

    @Override
    public BudgetResponse getBudgetById(String userId, UUID budgetId) {
        return budgetRepositoryPort.findById(userId, budgetId)
                .map(BudgetResponse::from)
                .orElse(null);
    }



    @Override
    public List<BudgetResponse> getBudgetsByPeriod(String userId, String period) {
        return budgetRepositoryPort.findByUserIdAndPeriod(userId, period).stream()
                .map(BudgetResponse::from)
                .toList();
    }
}
