package com.huynh.personal_expense_be.modules.category.presentation;

import com.huynh.personal_expense_be.modules.category.application.dto.*;
import com.huynh.personal_expense_be.modules.category.application.port.in.CreateCategoryUseCase;
import com.huynh.personal_expense_be.modules.category.application.port.in.DeleteCategoryUseCase;
import com.huynh.personal_expense_be.modules.category.application.port.in.GetCategoryAnalysisUseCase;
import com.huynh.personal_expense_be.modules.category.application.port.in.GetCategoryUseCase;
import com.huynh.personal_expense_be.modules.category.application.port.in.UpdateCategoryUseCase;
import com.huynh.personal_expense_be.modules.category.presentation.request.CreateCategoryRequest;
import com.huynh.personal_expense_be.modules.category.presentation.request.GetCategoryAnalysisRequest;
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
    private final GetCategoryAnalysisUseCase getCategoryAnalysisUseCase;
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
    public ResponseEntity<BaseResponse<CategoryResponse>> getById(@PathVariable UUID id, Principal principal) {
        return ResponseEntity.ok(
                BaseResponse.success("Category retrieved successfully", getCategoryUseCase.getCategoryById(id, principal.getName())));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<CategoryResponse>>> getAll(Principal principal) {
        return ResponseEntity.ok(
                BaseResponse.success("Categories retrieved successfully", getCategoryUseCase.getAllCategories(principal.getName())));
    }

    @GetMapping("/analysis")
    public ResponseEntity<BaseResponse<List<CategoryAnalysisResponse>>> getCategoryAnalysis(
            @ModelAttribute @Valid GetCategoryAnalysisRequest request,
            Principal principal) {

        String userId = principal.getName();
        GetCategoryAnalysisCommand command = new GetCategoryAnalysisCommand(
                userId,
                request.month(),
                request.year()
        );

        return ResponseEntity.ok(
                BaseResponse.success("Category analysis retrieved successfully",
                        getCategoryAnalysisUseCase.getCategoryAnalysis(command)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<CategoryResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request,
            Principal principal
        ) {
        UpdateCategoryCommand command = new UpdateCategoryCommand(
                request.name(),
                request.parentId(),
                principal.getName()
        );
        return ResponseEntity.ok(
                BaseResponse.success("Category updated successfully", updateCategoryUseCase.updateCategory(id, command)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable UUID id, Principal principal) {
        deleteCategoryUseCase.deleteCategory(principal.getName(), id);
        return ResponseEntity.accepted().build();
    }
}
