package com.huynh.personal_expense_be.modules.category.application.port.in;

import com.huynh.personal_expense_be.modules.category.application.dto.CategoryResponse;

import java.util.List;
import java.util.UUID;

public interface GetCategoryUseCase {

    CategoryResponse getCategoryById(UUID id);

    List<CategoryResponse> getAllCategories();
}
