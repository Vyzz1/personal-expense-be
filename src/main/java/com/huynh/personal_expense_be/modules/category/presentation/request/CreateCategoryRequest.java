package com.huynh.personal_expense_be.modules.category.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

@Schema(description = "Request body for creating a category")
public record CreateCategoryRequest(
        @Schema(description = "Category name", example = "Food & Dining")
        @NotBlank String name,

        @Schema(description = "Parent category ID for sub-categories. Omit for top-level categories.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID parentId
) {}
