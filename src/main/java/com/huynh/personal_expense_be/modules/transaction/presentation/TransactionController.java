package com.huynh.personal_expense_be.modules.transaction.presentation;

import com.huynh.personal_expense_be.modules.transaction.application.dto.CreateTransactionCommand;
import com.huynh.personal_expense_be.modules.transaction.application.dto.GetTransactionCommand;
import com.huynh.personal_expense_be.modules.transaction.application.dto.PageResult;
import com.huynh.personal_expense_be.modules.transaction.application.dto.TransactionResponse;
import com.huynh.personal_expense_be.modules.transaction.application.port.in.*;
import com.huynh.personal_expense_be.modules.transaction.presentation.request.GetTransactionRequest;
import com.huynh.personal_expense_be.modules.transaction.presentation.request.TransactionRequest;
import com.huynh.personal_expense_be.shared.response.BaseResponse;
import com.huynh.personal_expense_be.shared.response.PaginationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Manage income and expense transactions")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final CreateTransactionUseCase createTransactionUseCase;
    private final GetListTransactionUseCase getListTransactionUseCase;
    private final GetTransactionDetailUseCase getTransactionDetailUseCase;
    private final DeleteTransactionUseCase deleteTransactionUseCase;
    private final UpdateTransactionUseCase updateTransactionUseCase;

    @Operation(summary = "Create a transaction")
    @ApiResponse(responseCode = "200", description = "Transaction created")
    @PostMapping
    public ResponseEntity<BaseResponse<TransactionResponse>> createTransaction(
            @Valid @RequestBody TransactionRequest transactionRequest, Principal principal) {
        String userId = principal.getName();
        TransactionResponse response = createTransactionUseCase.createTransaction(
                new CreateTransactionCommand(
                        transactionRequest.description(),
                        transactionRequest.amount(),
                        transactionRequest.categoryId(),
                        transactionRequest.occurredAt(),
                        transactionRequest.type(),
                        userId
                )
        );
        return ResponseEntity.ok(BaseResponse.success("Transaction Created !", response));
    }

    @Operation(summary = "Get paginated list of transactions", description = "Supports filtering by date range, amount, category, type and keyword search")
    @ApiResponse(responseCode = "200", description = "Transactions retrieved")
    @GetMapping
    public ResponseEntity<BaseResponse<PaginationResponse<TransactionResponse>>> getAllTransactions(
            @ModelAttribute GetTransactionRequest request, Principal principal) {
        String userId = principal.getName();
        PageResult<TransactionResponse> result = getListTransactionUseCase.getListTransaction(
                new GetTransactionCommand(request.getPage(), request.getSize(), request.getSortBy(),
                        request.getSortOrder(), userId, request.getDescription(),
                        request.getCategoryIds(), request.getType(),
                        request.getFromDate(), request.getToDate(),
                        request.getMonth(), request.getYear(),
                        request.getMinAmount(), request.getMaxAmount()
                )
        );
        PaginationResponse<TransactionResponse> response = PaginationResponse.of(
                result.content(), result.page(), result.size(), result.totalElements(), result.totalPages(), result.last()
        );
        return ResponseEntity.ok(BaseResponse.success("Transactions retrieved successfully!", response));
    }

    @Operation(summary = "Get transaction by ID")
    @ApiResponse(responseCode = "200", description = "Transaction retrieved")
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<TransactionResponse>> getTransactionDetail(
            @PathVariable UUID id, Principal principal) {
        String userId = principal.getName();
        TransactionResponse response = getTransactionDetailUseCase.getTransactionDetailById(id, userId);
        return ResponseEntity.ok(BaseResponse.success("Transaction retrieved successfully !", response));
    }

    @Operation(summary = "Delete a transaction")
    @ApiResponse(responseCode = "200", description = "Transaction deleted")
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteTransaction(@PathVariable UUID id, Principal principal) {
        String userId = principal.getName();
        deleteTransactionUseCase.deleteTransactionById(userId, id);
        return ResponseEntity.ok(BaseResponse.success("Transaction deleted successfully !", null));
    }

    @Operation(summary = "Update a transaction")
    @ApiResponse(responseCode = "200", description = "Transaction updated")
    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<TransactionResponse>> updateTransaction(
            @PathVariable UUID id,
            @Valid @RequestBody TransactionRequest transactionRequest,
            Principal principal) {
        String userId = principal.getName();
        TransactionResponse response = updateTransactionUseCase.updateTransaction(id,
                new CreateTransactionCommand(
                        transactionRequest.description(),
                        transactionRequest.amount(),
                        transactionRequest.categoryId(),
                        transactionRequest.occurredAt(),
                        transactionRequest.type(),
                        userId
                )
        );
        return ResponseEntity.ok(BaseResponse.success("Transaction updated successfully !", response));
    }
}
