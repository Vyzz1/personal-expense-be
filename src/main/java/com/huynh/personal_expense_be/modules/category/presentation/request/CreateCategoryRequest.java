package com.huynh.personal_expense_be.modules.category.presentation.request;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateCategoryRequest(
        @NotBlank String name,
        UUID parentId
) {}
