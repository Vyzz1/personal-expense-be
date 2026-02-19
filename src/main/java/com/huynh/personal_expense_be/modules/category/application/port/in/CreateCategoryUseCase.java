package com.huynh.personal_expense_be.modules.category.application.port.in;

import com.huynh.personal_expense_be.modules.category.application.dto.CategoryResponse;
import com.huynh.personal_expense_be.modules.category.application.dto.CreateCategoryCommand;

public interface CreateCategoryUseCase {

    CategoryResponse createCategory(CreateCategoryCommand command);
}
