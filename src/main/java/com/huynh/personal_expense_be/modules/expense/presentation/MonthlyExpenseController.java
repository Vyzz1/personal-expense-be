package com.huynh.personal_expense_be.modules.expense.presentation;

import com.huynh.personal_expense_be.modules.expense.application.dto.GetMonthlyExpenseCommand;
import com.huynh.personal_expense_be.modules.expense.application.dto.GetMonthlyExpenseResponse;
import com.huynh.personal_expense_be.modules.expense.application.dto.GetThreeMonthCompareCommand;
import com.huynh.personal_expense_be.modules.expense.application.port.in.GetMonthlyExpenseUseCase;
import com.huynh.personal_expense_be.modules.expense.application.port.in.GetThreeMonthCompareUseCase;
import com.huynh.personal_expense_be.modules.expense.presentation.request.GetMonthlyExpenseRequest;
import com.huynh.personal_expense_be.shared.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RequestMapping("/api/v1/expenses")
@RestController
@RequiredArgsConstructor
public class MonthlyExpenseController {

    private final GetMonthlyExpenseUseCase getMonthlyExpenseUseCase;
    private final GetThreeMonthCompareUseCase getThreeMonthCompareUseCase;

    @GetMapping("/monthly")
    public BaseResponse<GetMonthlyExpenseResponse> getMonthlyExpense(@ModelAttribute GetMonthlyExpenseRequest request, Principal principal) {
        String userId = principal.getName();

        var command = new GetMonthlyExpenseCommand(
                userId,
                request.month(),
                request.year()
        );

        return BaseResponse.success("Monthly expense retrieved successfully", getMonthlyExpenseUseCase.getMonthlyExpense(command));
    }

    @GetMapping("/compare")
    public BaseResponse<List<GetMonthlyExpenseResponse>> getThreeMonthCompare(@ModelAttribute GetMonthlyExpenseRequest request, Principal principal) {
        String userId = principal.getName();

        var command = new GetThreeMonthCompareCommand(
                userId,
                request.month(),
                request.year()
        );

        return BaseResponse.success("Three month comparison retrieved successfully", getThreeMonthCompareUseCase.getThreeMonthCompare(command));
    }
}
