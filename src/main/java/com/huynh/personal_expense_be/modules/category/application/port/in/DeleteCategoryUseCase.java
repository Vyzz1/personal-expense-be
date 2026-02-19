package com.huynh.personal_expense_be.modules.category.application.port.in;

import java.util.UUID;

public interface DeleteCategoryUseCase {

    void deleteCategory(UUID id);
}
