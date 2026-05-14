package com.huynh.personal_expense_be.modules.budget.presentation;

import com.huynh.personal_expense_be.modules.budget.application.dto.BudgetResponse;
import com.huynh.personal_expense_be.modules.budget.application.dto.CreateBudgetCommand;
import com.huynh.personal_expense_be.modules.budget.application.dto.GetListBudgetCommand;
import com.huynh.personal_expense_be.modules.budget.application.dto.UpdateBudgetCommand;
import com.huynh.personal_expense_be.modules.budget.application.port.in.CreateBudgetUseCase;
import com.huynh.personal_expense_be.modules.budget.application.port.in.DeleteBudgetUseCase;
import com.huynh.personal_expense_be.modules.budget.application.port.in.GetBudgetUseCase;
import com.huynh.personal_expense_be.modules.budget.application.port.in.UpdateBudgetUseCase;
import com.huynh.personal_expense_be.modules.budget.presentation.request.CreateBudgetRequest;
import com.huynh.personal_expense_be.modules.budget.presentation.request.GetBudgetRequest;
import com.huynh.personal_expense_be.modules.budget.presentation.request.UpdateBudgetRequest;
import com.huynh.personal_expense_be.modules.transaction.application.dto.PageResult;
import com.huynh.personal_expense_be.shared.response.BaseResponse;
import com.huynh.personal_expense_be.shared.response.PaginationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final CreateBudgetUseCase createBudgetUseCase;
    private final GetBudgetUseCase getBudgetUseCase;
    private final DeleteBudgetUseCase deleteBudgetUseCase;
    private final UpdateBudgetUseCase updateBudgetUseCase;

    @PostMapping
    public ResponseEntity<BaseResponse<BudgetResponse>> create(
            Principal principal,
            @Valid @RequestBody CreateBudgetRequest request) {

        CreateBudgetCommand command = new CreateBudgetCommand(
                request.name(),
                principal.getName(),
                request.categoryId(),
                request.limitAmount(),
                request.thresholdPercentage()
        );

        BudgetResponse response = createBudgetUseCase.createBudget(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success("Budget created", response));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<PaginationResponse<BudgetResponse>>> getAll(Principal principal, @ModelAttribute GetBudgetRequest request) {

        GetListBudgetCommand command = new GetListBudgetCommand(
                request.getPage(),
                request.getSize(),
                request.getSortBy(),
                request.getSortOrder(),
                principal.getName(),
                request.getStatus(),
                request.getSearch(),
                request.getCategoryIds(),
                request.getPeriod()
        );

        PageResult<BudgetResponse> result = getBudgetUseCase.getListBudget(command);

        PaginationResponse<BudgetResponse> response = PaginationResponse.of(
                result.content(), result.page(), result.size(), result.totalElements(), result.totalPages(), result.last()
        );

        return ResponseEntity.ok(BaseResponse.success("Budgets retrieved", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<BudgetResponse>> getById(@PathVariable UUID id, Principal principal) {
        BudgetResponse resp = getBudgetUseCase.getBudgetById(principal.getName(), id);
        return ResponseEntity.ok(BaseResponse.success("Budget retrieved", resp));
    }

    @GetMapping("/period")
    public ResponseEntity<BaseResponse<List<BudgetResponse>>> getByPeriod(@RequestParam String period, Principal principal) {
        List<BudgetResponse> list = getBudgetUseCase.getBudgetsByPeriod(principal.getName(), period);
        return ResponseEntity.ok(BaseResponse.success("Budgets by period", list));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteBudgetUseCase.deleteBudget(id);
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<BudgetResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBudgetRequest request,
            Principal principal) {

        UpdateBudgetCommand command = new UpdateBudgetCommand(
                id,
                principal.getName(),
                request.name(),
                request.limitAmount()
        );

        BudgetResponse response = updateBudgetUseCase.updateBudget(command);

        return ResponseEntity.ok(BaseResponse.success("Budget updated", response));

    }
}
