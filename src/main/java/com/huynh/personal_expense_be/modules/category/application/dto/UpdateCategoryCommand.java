package com.huynh.personal_expense_be.modules.category.application.dto;

import java.util.UUID;

public record UpdateCategoryCommand(
        String name,
        UUID parentId
) {}
