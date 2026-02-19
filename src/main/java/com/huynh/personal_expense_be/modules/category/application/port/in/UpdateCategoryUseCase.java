package com.huynh.personal_expense_be.modules.category.application.port.in;

import com.huynh.personal_expense_be.modules.category.application.dto.CategoryResponse;
import com.huynh.personal_expense_be.modules.category.application.dto.UpdateCategoryCommand;

import java.util.UUID;

public interface UpdateCategoryUseCase {

    CategoryResponse updateCategory(UUID id, UpdateCategoryCommand command);
}
