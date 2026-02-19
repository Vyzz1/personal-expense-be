package com.huynh.personal_expense_be.modules.category.application.dto;

import java.util.UUID;

public record CreateCategoryCommand(
        String name,
        String userId,
        UUID parentId
) {}
