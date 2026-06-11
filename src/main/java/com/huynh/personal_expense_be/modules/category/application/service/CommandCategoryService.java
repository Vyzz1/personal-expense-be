package com.huynh.personal_expense_be.modules.category.application.service;

import com.huynh.personal_expense_be.modules.category.application.dto.CategoryResponse;
import com.huynh.personal_expense_be.modules.category.application.dto.CreateCategoryCommand;
import com.huynh.personal_expense_be.modules.category.application.dto.UpdateCategoryCommand;
import com.huynh.personal_expense_be.modules.category.application.port.in.CreateCategoryUseCase;
import com.huynh.personal_expense_be.modules.category.application.port.in.DeleteCategoryUseCase;
import com.huynh.personal_expense_be.modules.category.application.port.in.UpdateCategoryUseCase;
import com.huynh.personal_expense_be.modules.category.application.port.out.CategoryRepositoryPort;
import com.huynh.personal_expense_be.modules.category.domain.Category;
import com.huynh.personal_expense_be.shared.exception.BusinessValidationException;
import com.huynh.personal_expense_be.shared.exception.NotFoundException;
import com.huynh.personal_expense_be.shared.exception.ValidationFieldError;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommandCategoryService implements
        CreateCategoryUseCase,
        UpdateCategoryUseCase,
        DeleteCategoryUseCase {

    private final CategoryRepositoryPort categoryRepositoryPort;

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryCommand command) {
        if (categoryRepositoryPort.existsByNameAndUserId(command.name(), command.userId()).isPresent()) {

            List<ValidationFieldError> fieldErrors = List.of(ValidationFieldError.of(
                    "name",
                    command.name(),
                    "Category '" + command.name() + "' already exists for this user",
                    null
            ));

            throw new BusinessValidationException(fieldErrors);
        }

        if (command.parentId() != null) {
            if (categoryRepositoryPort.findByIdAndUserId(command.parentId(), command.userId()).isEmpty()) {
                List<ValidationFieldError> fieldErrors = List.of(ValidationFieldError.of(
                        "parentId",
                        command.parentId().toString(),
                        "Parent category not found",
                        null
                ));
                throw new BusinessValidationException(fieldErrors);
            }
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
    @Transactional
    public CategoryResponse updateCategory(UUID id, UpdateCategoryCommand command) {

        if(categoryRepositoryPort.existsByNameAndUserId(command.name(), command.userId())
                .filter(existing -> !existing.getId().equals(id))
                .isPresent()) {

            List<ValidationFieldError> fieldErrors = List.of(ValidationFieldError.of(
                    "name",
                    command.name(),
                    "Category '" + command.name() + "' already exists for this user",
                    null
            ));

            throw new BusinessValidationException(fieldErrors);
        }

        Category existing = categoryRepositoryPort.findByIdAndUserId(id, command.userId())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        Category updated = existing.toBuilder()
                .name(command.name())
                .parentId(command.parentId())
                .build();

        Category saved = categoryRepositoryPort.save(updated);
        return CategoryResponse.from(saved);
    }

    @Override
    @Transactional
    public void deleteCategory(String userId, UUID id) {
        Category existing = categoryRepositoryPort.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));
        if (!userId.equals(existing.getUserId())) {
            throw new NotFoundException("Category not found with id: " + id);
        }
        categoryRepositoryPort.deleteById(id);
    }
}
