package com.huynh.personal_expense_be.modules.expense.infrastructure.persistence;

import com.huynh.personal_expense_be.modules.category.infrastructure.persistence.CategoryJpaEntity;
import com.huynh.personal_expense_be.modules.expense.domain.MonthlyExpense;
import com.huynh.personal_expense_be.modules.transaction.infrastructure.persistence.TransactionJpaEntity;
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
                .transactionId(entity.getTransaction() != null ? entity.getTransaction().getId() : null)
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .isDeleted(entity.getIsDeleted())
                .build();
    }

    public MonthlyExpenseJpaEntity toJpaEntity(MonthlyExpense domain) {
        TransactionJpaEntity transactionRef = null;
        if (domain.getTransactionId() != null) {
            transactionRef = new TransactionJpaEntity();
            transactionRef.setId(domain.getTransactionId());
        }

        CategoryJpaEntity categoryRef = null;
        if (domain.getCategoryId() != null) {
            categoryRef = new CategoryJpaEntity();
            categoryRef.setId(domain.getCategoryId());
        }

        return MonthlyExpenseJpaEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .month(domain.getMonth())
                .year(domain.getYear())
                .totalAmount(domain.getTotalAmount())
                .previousTotalAmount(domain.getPreviousTotalAmount())
                .changePercentage(domain.getChangePercentage())
                .transaction(transactionRef)
                .category(categoryRef)
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .isDeleted(domain.getIsDeleted())
                .build();
    }
}
