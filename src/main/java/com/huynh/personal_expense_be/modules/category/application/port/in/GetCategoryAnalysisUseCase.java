package com.huynh.personal_expense_be.modules.category.application.port.in;

import com.huynh.personal_expense_be.modules.category.application.dto.CategoryAnalysisResponse;

import java.util.List;

public interface GetCategoryAnalysisUseCase {

    List<CategoryAnalysisResponse> getCategoryAnalysis(String userId);
}
