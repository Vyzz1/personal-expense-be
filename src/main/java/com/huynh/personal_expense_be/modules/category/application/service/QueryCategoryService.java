package com.huynh.personal_expense_be.modules.category.application.service;

import com.huynh.personal_expense_be.modules.category.application.dto.CategoryAnalysisResponse;
import com.huynh.personal_expense_be.modules.category.application.dto.CategoryResponse;
import com.huynh.personal_expense_be.modules.category.application.port.in.GetCategoryAnalysisUseCase;
import com.huynh.personal_expense_be.modules.category.application.port.in.GetCategoryUseCase;
import com.huynh.personal_expense_be.modules.category.application.port.out.CategoryRepositoryPort;
import com.huynh.personal_expense_be.modules.category.domain.Category;
import com.huynh.personal_expense_be.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QueryCategoryService implements GetCategoryUseCase, GetCategoryAnalysisUseCase {

    private final CategoryRepositoryPort categoryRepositoryPort;

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
    public List<CategoryAnalysisResponse> getCategoryAnalysis(String userId) {
        return categoryRepositoryPort.getCategoryAnalysis(userId).stream()
                .map(CategoryAnalysisResponse::from)
                .toList();
    }
}
