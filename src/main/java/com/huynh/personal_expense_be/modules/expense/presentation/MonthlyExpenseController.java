package com.huynh.personal_expense_be.modules.expense.presentation;

import com.huynh.personal_expense_be.modules.expense.application.dto.GetMonthlyExpenseCommand;
import com.huynh.personal_expense_be.modules.expense.application.dto.GetMonthlyExpenseResponse;
import com.huynh.personal_expense_be.modules.expense.application.dto.GetThreeMonthCompareCommand;
import com.huynh.personal_expense_be.modules.expense.application.port.in.GetMonthlyExpenseUseCase;
import com.huynh.personal_expense_be.modules.expense.application.port.in.GetThreeMonthCompareUseCase;
import com.huynh.personal_expense_be.modules.expense.presentation.request.GetMonthlyExpenseRequest;
import com.huynh.personal_expense_be.shared.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(name = "Expenses", description = "Monthly expense summaries and period comparisons")
@SecurityRequirement(name = "bearerAuth")
public class MonthlyExpenseController {

    private final GetMonthlyExpenseUseCase getMonthlyExpenseUseCase;
    private final GetThreeMonthCompareUseCase getThreeMonthCompareUseCase;

    @Operation(summary = "Get monthly expense summary", description = "Returns total spending and change percentage vs previous month")
    @ApiResponse(responseCode = "200", description = "Monthly expense retrieved")
    @GetMapping("/monthly")
    public BaseResponse<GetMonthlyExpenseResponse> getMonthlyExpense(
            @Valid @ModelAttribute GetMonthlyExpenseRequest request, Principal principal) {
        String userId = principal.getName();
        var command = new GetMonthlyExpenseCommand(userId, request.month(), request.year());
        return BaseResponse.success("Monthly expense retrieved successfully", getMonthlyExpenseUseCase.getMonthlyExpense(command));
    }

    @Operation(summary = "Get three-month expense comparison", description = "Returns expense data for the given month and the two preceding months")
    @ApiResponse(responseCode = "200", description = "Comparison retrieved")
    @GetMapping("/compare")
    public BaseResponse<List<GetMonthlyExpenseResponse>> getThreeMonthCompare(
            @Valid @ModelAttribute GetMonthlyExpenseRequest request, Principal principal) {
        String userId = principal.getName();
        var command = new GetThreeMonthCompareCommand(userId, request.month(), request.year());
        return BaseResponse.success("Three month comparison retrieved successfully", getThreeMonthCompareUseCase.getThreeMonthCompare(command));
    }
}
