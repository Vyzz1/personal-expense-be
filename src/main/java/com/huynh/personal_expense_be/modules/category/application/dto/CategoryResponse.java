package com.huynh.personal_expense_be.modules.category.application.dto;

import com.huynh.personal_expense_be.modules.category.domain.Category;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String userId,
        UUID parentId,
        List<CategoryResponse> children,
        Instant createdAt,
        Instant updatedAt
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getUserId(),
                category.getParentId(),
                new ArrayList<>(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
