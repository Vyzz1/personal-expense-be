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

import java.util.*;

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
    public List<CategoryResponse> getAllCategories(String userId) {
        List<Category> allCategories = categoryRepositoryPort.findAllByUserId(userId);

        Map<UUID, CategoryResponse> map = new LinkedHashMap<>();

        // map all
        for (Category c : allCategories) {
            map.put(c.getId(), CategoryResponse.from(c));
        }
        // build tree
        List<CategoryResponse> roots = new ArrayList<>();

        for (CategoryResponse node : map.values()) {
            if (node.parentId() == null) {
                roots.add(node);
            } else {
                CategoryResponse parent = map.get(node.parentId());
                if (parent != null) {
                    parent.children().add(node);
                } else {
                    roots.add(node);
                }
            }
        }

        List<CategoryResponse> result = new ArrayList<>();
        for (CategoryResponse root : roots) {
            dfs(root, result);
        }

        result.sort(Comparator.comparing(CategoryResponse::createdAt));
        return result;
    }

    private void dfs(CategoryResponse node, List<CategoryResponse> result) {
        result.add(node);
        for (CategoryResponse child : node.children()) {
            dfs(child, result);
        }
    }

    @Override
    public List<CategoryAnalysisResponse> getCategoryAnalysis(String userId) {
        return categoryRepositoryPort.getCategoryAnalysis(userId).stream()
                .map(CategoryAnalysisResponse::from)
                .toList();
    }
}
