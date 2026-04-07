package com.huynh.personal_expense_be.modules.category.application.port.in;

import com.huynh.personal_expense_be.modules.category.application.dto.CategoryAnalysisResponse;
import com.huynh.personal_expense_be.modules.category.application.dto.GetCategoryAnalysisCommand;

import java.util.List;

public interface GetCategoryAnalysisUseCase {

    List<CategoryAnalysisResponse> getCategoryAnalysis(GetCategoryAnalysisCommand command);
}
