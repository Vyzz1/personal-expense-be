package com.huynh.personal_expense_be.modules.budget.presentation.request;

import com.huynh.personal_expense_be.modules.budget.domain.BudgetStatus;
import com.huynh.personal_expense_be.shared.request.CommonPageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class GetBudgetRequest extends CommonPageRequest {

    private BudgetStatus status;
    private String search;
    private List<UUID> categoryIds;
    private YearMonth period;


}
