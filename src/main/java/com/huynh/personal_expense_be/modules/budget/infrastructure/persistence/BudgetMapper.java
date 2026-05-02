package com.huynh.personal_expense_be.modules.budget.infrastructure.persistence;

import com.huynh.personal_expense_be.modules.budget.domain.Budget;
import com.huynh.personal_expense_be.modules.category.infrastructure.persistence.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BudgetMapper {

    private final CategoryMapper categoryMapper;

    public  BudgetJpaEntity toEntity(Budget budget) {
        return BudgetJpaEntity.builder()
                .id(budget.getId())
                .name(budget.getName())
                .category(budget.getCategory() != null ? categoryMapper.toJpaEntity(budget.getCategory()) : null)
                .userId(budget.getUserId())
                .limitAmount(budget.getLimitAmount())
                .spentAmount(budget.getSpentAmount())
                .period(budget.getPeriod())
                .createdAt(budget.getCreatedAt())
                .updatedAt(budget.getUpdatedAt())
                .isDeleted(budget.getIsDeleted())
                .status(budget.getStatus())
                .build();
    }

    public  Budget toDomain(BudgetJpaEntity entity) {
        return Budget.builder()
                .id(entity.getId())
                .name(entity.getName())
                .category(entity.getCategory() != null ? categoryMapper.toDomain(entity.getCategory()) : null)
                .userId(entity.getUserId())
                .limitAmount(entity.getLimitAmount())
                .spentAmount(entity.getSpentAmount())
                .period(entity.getPeriod())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .isDeleted(entity.getIsDeleted())
                .build();
    }
}
