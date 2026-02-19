package com.huynh.personal_expense_be.modules.category.presentation;

import com.huynh.personal_expense_be.modules.category.application.dto.CategoryResponse;
import com.huynh.personal_expense_be.modules.category.application.dto.CreateCategoryCommand;
import com.huynh.personal_expense_be.modules.category.application.dto.UpdateCategoryCommand;
import com.huynh.personal_expense_be.modules.category.application.port.in.CreateCategoryUseCase;
import com.huynh.personal_expense_be.modules.category.application.port.in.DeleteCategoryUseCase;
import com.huynh.personal_expense_be.modules.category.application.port.in.GetCategoryUseCase;
import com.huynh.personal_expense_be.modules.category.application.port.in.UpdateCategoryUseCase;
import com.huynh.personal_expense_be.modules.category.presentation.request.CreateCategoryRequest;
import com.huynh.personal_expense_be.modules.category.presentation.request.UpdateCategoryRequest;
import com.huynh.personal_expense_be.shared.response.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CreateCategoryUseCase createCategoryUseCase;
    private final GetCategoryUseCase getCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final DeleteCategoryUseCase deleteCategoryUseCase;

    @PostMapping
    public ResponseEntity<BaseResponse<CategoryResponse>> create(
            Principal principal,
            @Valid @RequestBody CreateCategoryRequest request) {
        CreateCategoryCommand command = new CreateCategoryCommand(
                request.name(),
                principal.getName(),
                request.parentId()
        );
        CategoryResponse response = createCategoryUseCase.createCategory(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success("Category created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<CategoryResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(
                BaseResponse.success("Category retrieved successfully", getCategoryUseCase.getCategoryById(id)));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<CategoryResponse>>> getAll() {
        return ResponseEntity.ok(
                BaseResponse.success("Categories retrieved successfully", getCategoryUseCase.getAllCategories()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<CategoryResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        UpdateCategoryCommand command = new UpdateCategoryCommand(
                request.name(),
                request.parentId()
        );
        return ResponseEntity.ok(
                BaseResponse.success("Category updated successfully", updateCategoryUseCase.updateCategory(id, command)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable UUID id) {
        deleteCategoryUseCase.deleteCategory(id);
        return ResponseEntity.accepted().build();
    }
}
