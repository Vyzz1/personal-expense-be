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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Categories", description = "Manage expense categories with optional hierarchy (parent/child)")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CreateCategoryUseCase createCategoryUseCase;
    private final GetCategoryUseCase getCategoryUseCase;
    private final GetCategoryAnalysisUseCase getCategoryAnalysisUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final DeleteCategoryUseCase deleteCategoryUseCase;

    @Operation(summary = "Create a category")
    @ApiResponse(responseCode = "201", description = "Category created")
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

    @Operation(summary = "Get category by ID")
    @ApiResponse(responseCode = "200", description = "Category retrieved")
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<CategoryResponse>> getById(@PathVariable UUID id, Principal principal) {
        return ResponseEntity.ok(
                BaseResponse.success("Category retrieved successfully", getCategoryUseCase.getCategoryById(id, principal.getName())));
    }

    @Operation(summary = "Get all categories for current user")
    @ApiResponse(responseCode = "200", description = "Categories retrieved")
    @GetMapping
    public ResponseEntity<BaseResponse<List<CategoryResponse>>> getAll(Principal principal) {
        return ResponseEntity.ok(
                BaseResponse.success("Categories retrieved successfully", getCategoryUseCase.getAllCategories(principal.getName())));
    }

    @Operation(summary = "Get category spending analysis by month/year")
    @ApiResponse(responseCode = "200", description = "Analysis retrieved")
    @GetMapping("/analysis")
    public ResponseEntity<BaseResponse<List<CategoryAnalysisResponse>>> getCategoryAnalysis(
            @ModelAttribute @Valid GetCategoryAnalysisRequest request,
            Principal principal) {
        String userId = principal.getName();
        GetCategoryAnalysisCommand command = new GetCategoryAnalysisCommand(userId, request.month(), request.year());
        return ResponseEntity.ok(
                BaseResponse.success("Category analysis retrieved successfully",
                        getCategoryAnalysisUseCase.getCategoryAnalysis(command)));
    }

    @Operation(summary = "Update a category")
    @ApiResponse(responseCode = "200", description = "Category updated")
    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<CategoryResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request,
            Principal principal) {
        UpdateCategoryCommand command = new UpdateCategoryCommand(
                request.name(),
                request.parentId(),
                principal.getName()
        );
        return ResponseEntity.ok(
                BaseResponse.success("Category updated successfully", updateCategoryUseCase.updateCategory(id, command)));
    }

    @Operation(summary = "Delete a category")
    @ApiResponse(responseCode = "202", description = "Category deleted")
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable UUID id, Principal principal) {
        deleteCategoryUseCase.deleteCategory(principal.getName(), id);
        return ResponseEntity.accepted().build();
    }
}
