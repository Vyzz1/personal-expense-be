package com.huynh.personal_expense_be.modules.category.application.service;

import com.huynh.personal_expense_be.modules.category.application.dto.CategoryResponse;
import com.huynh.personal_expense_be.modules.category.application.dto.CreateCategoryCommand;
import com.huynh.personal_expense_be.modules.category.application.dto.UpdateCategoryCommand;
import com.huynh.personal_expense_be.modules.category.application.port.in.CreateCategoryUseCase;
import com.huynh.personal_expense_be.modules.category.application.port.in.DeleteCategoryUseCase;
import com.huynh.personal_expense_be.modules.category.application.port.in.GetCategoryUseCase;
import com.huynh.personal_expense_be.modules.category.application.port.in.UpdateCategoryUseCase;
import com.huynh.personal_expense_be.modules.category.application.port.out.CategoryRepositoryPort;
import com.huynh.personal_expense_be.modules.category.domain.Category;
import com.huynh.personal_expense_be.shared.exception.DuplicateException;
import com.huynh.personal_expense_be.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService implements
        CreateCategoryUseCase,
        GetCategoryUseCase,
        UpdateCategoryUseCase,
        DeleteCategoryUseCase {

    private final CategoryRepositoryPort categoryRepositoryPort;

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryCommand command) {
        if (categoryRepositoryPort.existsByNameAndUserId(command.name(), command.userId())) {
            throw new DuplicateException("Category '" + command.name() + "' already exists for this user");
        }
        Category category = Category.builder()
                .name(command.name())
                .userId(command.userId())
                .parentId(command.parentId())
                .build();

        Category saved = categoryRepositoryPort.save(category);
        return CategoryResponse.from(saved);
    }

    @Override
    public CategoryResponse getCategoryById(UUID id) {
        Category category = categoryRepositoryPort.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));
        return CategoryResponse.from(category);
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepositoryPort.findAll()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(UUID id, UpdateCategoryCommand command) {
        Category existing = categoryRepositoryPort.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));

        Category updated = existing.toBuilder()
                .name(command.name())
                .parentId(command.parentId())
                .build();

        Category saved = categoryRepositoryPort.save(updated);
        return CategoryResponse.from(saved);
    }

    @Override
    @Transactional
    public void deleteCategory(UUID id) {
        if (!categoryRepositoryPort.existsById(id)) {
            throw new NotFoundException("Category not found with id: " + id);
        }
        categoryRepositoryPort.deleteById(id);
    }
}
