package com.huynh.personal_expense_be.modules.transaction.infrastructure.persistence;

import com.huynh.personal_expense_be.modules.category.domain.Category;
import com.huynh.personal_expense_be.modules.category.infrastructure.persistence.CategoryJpaEntity;
import com.huynh.personal_expense_be.modules.transaction.domain.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public Transaction toDomain(TransactionJpaEntity entity) {
        return Transaction.builder()
                .id(entity.getId())
                .amount(entity.getAmount())
                .type(entity.getType())
                .category(toCategoryDomain(entity.getCategory()))
                .description(entity.getDescription())
                .occurredAt(entity.getOccurredAt())
                .userId(entity.getUserId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .isDeleted(entity.getIsDeleted())
                .build();
    }

    public Category toCategoryDomain(CategoryJpaEntity entity) {
        if (entity == null) return null;
        return Category.builder()
                .id(entity.getId())
                .name(entity.getName())
                .userId(entity.getUserId())
                .parentId(entity.getParentId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .isDeleted(entity.getIsDeleted())
                .build();
    }

    public  CategoryJpaEntity toCategoryJpaEntity(Category domain) {
        return new CategoryJpaEntity(
                domain.getId(),
                domain.getName(),
                domain.getUserId(),
                domain.getParentId(),
                domain.getCreatedAt(),
                domain.getUpdatedAt(),
                domain.getIsDeleted()
        );
    }

    public  TransactionJpaEntity toJpaEntity(Transaction domain) {
        return TransactionJpaEntity.builder()
                .id(domain.getId())
                .amount(domain.getAmount())
                .type(domain.getType())
                .category(toCategoryJpaEntity(domain.getCategory()))
                .description(domain.getDescription())
                .occurredAt(domain.getOccurredAt())
                .userId(domain.getUserId())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .isDeleted(domain.getIsDeleted())
                .build();
    }
}
