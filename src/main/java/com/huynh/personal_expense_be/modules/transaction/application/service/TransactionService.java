package com.huynh.personal_expense_be.modules.transaction.application.service;

import com.huynh.personal_expense_be.modules.category.application.port.out.CategoryRepositoryPort;
import com.huynh.personal_expense_be.modules.category.domain.Category;
import com.huynh.personal_expense_be.modules.transaction.application.dto.CreateTransactionCommand;
import com.huynh.personal_expense_be.modules.transaction.application.dto.TransactionResponse;
import com.huynh.personal_expense_be.modules.transaction.application.port.in.*;
import com.huynh.personal_expense_be.modules.transaction.application.port.out.TransactionRepositoryPort;
import com.huynh.personal_expense_be.modules.transaction.domain.Transaction;
import com.huynh.personal_expense_be.modules.transaction.domain.TransactionType;
import com.huynh.personal_expense_be.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService  implements CreateTransactionUseCase, GetListTransactionUseCase, GetTransactionDetailUseCase , DeleteTransactionUseCase , UpdateTransactionUseCase {

    private final CategoryRepositoryPort categoryRepositoryPort;
    private final TransactionRepositoryPort transactionRepositoryPort;

    @Override
    @Transactional
    public TransactionResponse createTransaction(CreateTransactionCommand command) {

        Category category =  categoryRepositoryPort.findById(command.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + command.categoryId()));


        Transaction transaction = Transaction.builder()
                .amount(command.amount())
                .description(command.description())
                .occurredAt(command.occurredAt() != null ? command.occurredAt() : Instant.now())
                .userId(command.userId())
                .type(command.type() != null ? command.type() : TransactionType.EXPENSE)
                .category(category)
                .build();

        return TransactionResponse.from(transactionRepositoryPort.save(transaction));
    }

    @Override
    public List<TransactionResponse> getListTransaction(String userId) {

        return transactionRepositoryPort.findByUserId(userId).stream()
                .map(TransactionResponse::from)
                .toList();
    }

    @Override
    public TransactionResponse getTransactionDetailById(
            UUID transactionId, String userId) {
        Transaction transaction = transactionRepositoryPort.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("Transaction not found with id: " + transactionId));



        if (!transaction.getUserId().equals(userId)) {
            throw new NotFoundException("Transaction not found with id: " + transactionId);
        }

        return TransactionResponse.from(transaction);
    }


    @Transactional
    @Override
    public void deleteTransactionById(String userId, UUID transactionId) {
        Transaction transaction = transactionRepositoryPort.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("Transaction not found with id: " + transactionId));


        if (!transaction.getUserId().equals(userId)) {
            throw new NotFoundException("Transaction not found with id: " + transactionId);
        }
        transactionRepositoryPort.deleteById(transactionId);
    }

    @Override
    @Transactional
    public TransactionResponse updateTransaction(UUID transactionId, CreateTransactionCommand command) {
        Transaction transaction = transactionRepositoryPort.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("Transaction not found with id: " + transactionId));

        if (!transaction.getUserId().equals(command.userId())) {
            throw new NotFoundException("Transaction not found with id: " + transactionId);
        }

        Category category =  categoryRepositoryPort.findById(command.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + command.categoryId()));

        Transaction updated = transaction.toBuilder()
                .amount(command.amount())
                .description(command.description())
                .occurredAt(command.occurredAt() != null ? command.occurredAt() : transaction.getOccurredAt())
                .type(command.type() != null ? command.type() : transaction.getType())
                .category(category)
                .build();

        return TransactionResponse.from(transactionRepositoryPort.save(updated));
    }


}
