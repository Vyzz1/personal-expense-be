package com.huynh.personal_expense_be.modules.transaction.presentation;

import com.huynh.personal_expense_be.modules.transaction.application.dto.CreateTransactionCommand;
import com.huynh.personal_expense_be.modules.transaction.application.dto.TransactionResponse;
import com.huynh.personal_expense_be.modules.transaction.application.port.in.*;
import com.huynh.personal_expense_be.modules.transaction.presentation.request.TransactionRequest;
import com.huynh.personal_expense_be.shared.response.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final CreateTransactionUseCase createTransactionUseCase;
    private final GetListTransactionUseCase getListTransactionUseCase;
    private final GetTransactionDetailUseCase getTransactionDetailUseCase;
    private final DeleteTransactionUseCase deleteTransactionUseCase;
    private final UpdateTransactionUseCase updateTransactionUseCase;

    @PostMapping
    public ResponseEntity<BaseResponse<TransactionResponse>> createTransaction(@Valid @RequestBody TransactionRequest transactionRequest , Principal principal) {

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
        return ResponseEntity.ok(BaseResponse.success("Transaction Created !",response));

    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<TransactionResponse>>> getAllTransactions(Principal principal) {

        String userId = principal.getName();

        List<TransactionResponse> transactions = getListTransactionUseCase.getListTransaction(userId);

        return ResponseEntity.ok(BaseResponse.success("Transactions retrieved successfully !", transactions));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<TransactionResponse>> getTransactionDetail(
            @PathVariable UUID id, Principal principal) {
        String userId = principal.getName();
        TransactionResponse response = getTransactionDetailUseCase.getTransactionDetailById(id, userId);
        return ResponseEntity.ok(BaseResponse.success("Transaction retrieved successfully !", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteTransaction (@PathVariable UUID id, Principal principal) {
        String userId = principal.getName();
        deleteTransactionUseCase.deleteTransactionById(userId, id);
        return ResponseEntity.ok(BaseResponse.success("Transaction deleted successfully !", null));

    }

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
