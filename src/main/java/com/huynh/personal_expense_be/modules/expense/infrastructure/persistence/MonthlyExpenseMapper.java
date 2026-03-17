package com.huynh.personal_expense_be.modules.expense.infrastructure.persistence;

import com.huynh.personal_expense_be.modules.expense.domain.MonthlyExpense;
import org.springframework.stereotype.Component;

@Component
public class MonthlyExpenseMapper {

    public MonthlyExpense toDomain(MonthlyExpenseJpaEntity entity) {
        return MonthlyExpense.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .month(entity.getMonth())
                .year(entity.getYear())
                .totalAmount(entity.getTotalAmount())
                .previousTotalAmount(entity.getPreviousTotalAmount())
                .changePercentage(entity.getChangePercentage())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .isDeleted(entity.getIsDeleted())
                .build();
    }

    public MonthlyExpenseJpaEntity toJpaEntity(MonthlyExpense domain) {



        return MonthlyExpenseJpaEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .month(domain.getMonth())
                .year(domain.getYear())
                .totalAmount(domain.getTotalAmount())
                .previousTotalAmount(domain.getPreviousTotalAmount())
                .changePercentage(domain.getChangePercentage())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .isDeleted(domain.getIsDeleted())
                .build();
    }
}
