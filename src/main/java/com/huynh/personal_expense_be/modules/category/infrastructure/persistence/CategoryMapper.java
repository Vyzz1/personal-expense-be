package com.huynh.personal_expense_be.modules.category.infrastructure.persistence;

import com.huynh.personal_expense_be.modules.category.domain.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toDomain(CategoryJpaEntity entity) {
        return toDomain(entity, null);
    }

    public Category toDomain(CategoryJpaEntity entity, Category parent) {
        return Category.builder()
                .id(entity.getId())
                .name(entity.getName())
                .userId(entity.getUserId())
                .parentId(entity.getParentId())
                .parent(parent)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .isDeleted(entity.getIsDeleted())
                .build();
    }

    public CategoryJpaEntity toJpaEntity(Category domain) {
        return CategoryJpaEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .userId(domain.getUserId())
                .parentId(domain.getParentId())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .isDeleted(domain.getIsDeleted())
                .build();
    }
}
