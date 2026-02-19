package com.huynh.personal_expense_be.modules.category.infrastructure.persistence;

import com.huynh.personal_expense_be.modules.category.domain.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toDomain(CategoryJpaEntity entity) {
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

    public CategoryJpaEntity toJpaEntity(Category domain) {
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
}
