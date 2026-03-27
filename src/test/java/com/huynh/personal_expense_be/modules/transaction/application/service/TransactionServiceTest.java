package com.huynh.personal_expense_be.modules.transaction.application.service;

import com.huynh.personal_expense_be.modules.category.application.port.out.CategoryRepositoryPort;
import com.huynh.personal_expense_be.modules.category.domain.Category;
import com.huynh.personal_expense_be.modules.transaction.application.dto.CreateTransactionCommand;
import com.huynh.personal_expense_be.modules.transaction.application.dto.GetTransactionCommand;
import com.huynh.personal_expense_be.modules.transaction.application.dto.PageResult;
import com.huynh.personal_expense_be.modules.transaction.application.dto.TransactionResponse;
import com.huynh.personal_expense_be.modules.transaction.application.port.out.TransactionRepositoryPort;
import com.huynh.personal_expense_be.modules.transaction.domain.Transaction;
import com.huynh.personal_expense_be.modules.transaction.domain.TransactionType;
import com.huynh.personal_expense_be.modules.transaction.domain.event.TransactionCreatedEvent;
import com.huynh.personal_expense_be.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private CategoryRepositoryPort categoryRepositoryPort;

    @Mock
    private TransactionRepositoryPort transactionRepositoryPort;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private final String userId = "user-1";

    Transaction transaction;

    Category category;

    private final UUID transactionId = UUID.randomUUID();

    private final UUID categoryId = UUID.randomUUID();

    @BeforeEach
    void setUp() {

         category = Category.builder()
                .id(categoryId)
                .name("Food")
                .userId(userId)
                .parentId(null)
                .build();

         transaction = Transaction.builder()
                .id(transactionId)
                .userId(userId)
                .amount(BigDecimal.valueOf(100.00))
                .type(TransactionType.EXPENSE)
                .category(category)
                .description("Test transaction")
                .build();

    }

    @Test
    void  createTransaction_success() {
        CreateTransactionCommand command = new CreateTransactionCommand(
                "Test transaction",
                BigDecimal.valueOf(100.00),
                category.getId(),
                Instant.now(),
                TransactionType.EXPENSE,
                userId
        );

        when(categoryRepositoryPort.findById(category.getId())).thenReturn(Optional.of(category));
        when(transactionRepositoryPort.save(any(Transaction.class))).thenReturn(transaction);

        TransactionResponse response = transactionService.createTransaction(command);

        verify(categoryRepositoryPort).findById(category.getId());
        verify(transactionRepositoryPort).save(any(Transaction.class));

        TransactionCreatedEvent expectedEvent = new TransactionCreatedEvent(
                transaction.getId(),
                transaction.getUserId(),
                transaction.getCategory().getId(),
                transaction.getAmount(),
                transaction.getOccurredAt()
        );

        verify(applicationEventPublisher).publishEvent(expectedEvent);

        assertEquals(command.description(), response.description());
        assertEquals(command.amount(), response.amount());
        assertEquals(command.categoryId(), response.category().id());


    }


    @Test
    void  createTransaction_success_null_values() {

        Transaction transactionWithNullOccurredAt = Transaction.builder()
                .id(transactionId)
                .userId(userId)
                .amount(BigDecimal.valueOf(100.00))
                .type(null)
                .category(category)
                .description("Test transaction")
                .occurredAt(null)
                .build();

        CreateTransactionCommand command = new CreateTransactionCommand(
                "Test transaction",
                BigDecimal.valueOf(100.00),
                category.getId(),
                Instant.now(),
                TransactionType.EXPENSE,
                userId
        );

        when(categoryRepositoryPort.findById(category.getId())).thenReturn(Optional.of(category));
        when(transactionRepositoryPort.save(any(Transaction.class))).thenReturn(transaction);

        TransactionResponse response = transactionService.createTransaction(command);

        verify(categoryRepositoryPort).findById(category.getId());
        verify(transactionRepositoryPort).save(any(Transaction.class));

        TransactionCreatedEvent expectedEvent = new TransactionCreatedEvent(
                transactionWithNullOccurredAt.getId(),
                transactionWithNullOccurredAt.getUserId(),
                transactionWithNullOccurredAt.getCategory().getId(),
                transactionWithNullOccurredAt.getAmount(),
                transaction.getOccurredAt()
        );

        verify(applicationEventPublisher).publishEvent(expectedEvent);

        assertEquals(command.description(), response.description());
        assertEquals(command.amount(), response.amount());
        assertEquals(command.categoryId(), response.category().id());


    }

   

    @Test
    void  createTransaction_categoryNotFound() {
        CreateTransactionCommand command = new CreateTransactionCommand(
                "Test transaction",
                BigDecimal.valueOf(100.00),
                category.getId(),
                Instant.now(),
                TransactionType.EXPENSE,
                userId
        );

        when(categoryRepositoryPort.findById(category.getId())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            transactionService.createTransaction(command);
        });

        assertEquals("Category not found with id: " + category.getId(), exception.getMessage());
        verify(categoryRepositoryPort).findById(category.getId());
        verify(transactionRepositoryPort, never()).save(any(Transaction.class));
        verify(applicationEventPublisher, never()).publishEvent(any(TransactionCreatedEvent.class));
    }

    @Test
    void getTransactionDetailById_success() {
        when(transactionRepositoryPort.findById(transactionId)).thenReturn(Optional.of(transaction));

        TransactionResponse response = transactionService.getTransactionDetailById(transactionId, userId);

        verify(transactionRepositoryPort).findById(transactionId);

        assertEquals(transaction.getId().toString(), response.id());
        assertEquals(transaction.getDescription(), response.description());
        assertEquals(transaction.getAmount(), response.amount());
        assertEquals(transaction.getCategory().getId().toString(), response.category().id().toString());
        assertEquals(transaction.getOccurredAt(), response.occurredAt());
        assertEquals(transaction.getType().toString(), response.type());

    }

    @Test
    void getTransactionDetailById_transactionNotFound() {
        when(transactionRepositoryPort.findById(transactionId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            transactionService.getTransactionDetailById(transactionId, userId);
        });

        assertEquals("Transaction not found with id: " + transactionId, exception.getMessage());
        verify(transactionRepositoryPort).findById(transactionId);
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    void deleteTransactionById_success() {
        when(transactionRepositoryPort.findById(transactionId)).thenReturn(Optional.of(transaction));

        transactionService.deleteTransactionById(userId, transactionId);

        verify(transactionRepositoryPort).findById(transactionId);
        verify(transactionRepositoryPort).deleteById(transactionId);

    }

    @Test
    void deleteTransactionById_transactionNotFound() {
        when(transactionRepositoryPort.findById(transactionId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            transactionService.deleteTransactionById(userId, transactionId);
        });

        assertEquals("Transaction not found with id: " + transactionId, exception.getMessage());
        verify(transactionRepositoryPort).findById(transactionId);
        verify(transactionRepositoryPort, never()).deleteById(transactionId);
        verify(applicationEventPublisher, never()).publishEvent(any());

    }

    @Test
    void deleteTransactionById_transactionBelongsToAnotherUser() {
        Transaction otherUserTransaction = Transaction.builder()
                .id(transactionId)
                .userId("another-user")
                .amount(BigDecimal.valueOf(100.00))
                .type(TransactionType.EXPENSE)
                .category(category)
                .description("Test transaction")
                .build();

        when(transactionRepositoryPort.findById(transactionId)).thenReturn(Optional.of(otherUserTransaction));

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            transactionService.deleteTransactionById(userId, transactionId);
        });

        assertEquals("Transaction not found with id: " + transactionId, exception.getMessage());
        verify(transactionRepositoryPort).findById(transactionId);
        verify(transactionRepositoryPort, never()).deleteById(transactionId);
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    void getTransactionDetailById_transactionBelongsToAnotherUser() {
        Transaction otherUserTransaction = Transaction.builder()
                .id(transactionId)
                .userId("another-user")
                .amount(BigDecimal.valueOf(100.00))
                .type(TransactionType.EXPENSE)
                .category(category)
                .description("Test transaction")
                .build();

        when(transactionRepositoryPort.findById(transactionId)).thenReturn(Optional.of(otherUserTransaction));

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            transactionService.getTransactionDetailById(transactionId, userId);
        });

        assertEquals("Transaction not found with id: " + transactionId, exception.getMessage());
        verify(transactionRepositoryPort).findById(transactionId);
    }

    @Test
    void updateTransaction_success() {
        CreateTransactionCommand command = new CreateTransactionCommand(
                "Updated transaction",
                BigDecimal.valueOf(200.00),
                category.getId(),
                Instant.now(),
                TransactionType.EXPENSE,
                userId
        );

        when(transactionRepositoryPort.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(categoryRepositoryPort.findById(category.getId())).thenReturn(Optional.of(category));

        Transaction updatedTransaction = transaction.toBuilder()
            .amount(command.amount())
            .description(command.description())
            .occurredAt(command.occurredAt())
            .type(command.type())
            .category(category)
            .build();

        when(transactionRepositoryPort.save(any(Transaction.class))).thenReturn(updatedTransaction);

        TransactionResponse response = transactionService.updateTransaction(transactionId, command);

        verify(transactionRepositoryPort).findById(transactionId);
        verify(categoryRepositoryPort).findById(category.getId());
        verify(transactionRepositoryPort).save(any(Transaction.class));

        assertEquals(command.description(), response.description());
        assertEquals(command.amount(), response.amount());
        assertEquals(command.categoryId(), response.category().id());
    }

      @Test
    void updateTransaction_success_null_values() {
        CreateTransactionCommand command = new CreateTransactionCommand(
                "Updated transaction",
                BigDecimal.valueOf(200.00),
                category.getId(),
                null,
                null,
                userId
        );

        when(transactionRepositoryPort.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(categoryRepositoryPort.findById(category.getId())).thenReturn(Optional.of(category));

        Transaction updatedTransaction = transaction.toBuilder()
            .amount(command.amount())
            .description(command.description())
            .occurredAt(command.occurredAt() != null ? command.occurredAt() : transaction.getOccurredAt())
            .type(command.type() != null ? command.type() : transaction.getType())
            .category(category)
            .build();

        when(transactionRepositoryPort.save(any(Transaction.class))).thenReturn(updatedTransaction);

        TransactionResponse response = transactionService.updateTransaction(transactionId, command);

        verify(transactionRepositoryPort).findById(transactionId);
        verify(categoryRepositoryPort).findById(category.getId());
        verify(transactionRepositoryPort).save(any(Transaction.class));

        assertEquals(command.description(), response.description());
        assertEquals(command.amount(), response.amount());
        assertEquals(command.categoryId(), response.category().id());
    }

    @Test
    void updateTransaction_transactionNotFound() {
        CreateTransactionCommand command = new CreateTransactionCommand(
                "Updated transaction",
                BigDecimal.valueOf(200.00),
                category.getId(),
                Instant.now(),
                TransactionType.EXPENSE,
                userId
        );

        when(transactionRepositoryPort.findById(transactionId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            transactionService.updateTransaction(transactionId, command);
        });

        assertEquals("Transaction not found with id: " + transactionId, exception.getMessage());
        verify(transactionRepositoryPort).findById(transactionId);
        verify(categoryRepositoryPort, never()).findById(any());
        verify(transactionRepositoryPort, never()).save(any());
    }

    @Test
    void updateTransaction_transactionBelongsToAnotherUser() {
        Transaction otherUserTransaction = Transaction.builder()
                .id(transactionId)
                .userId("another-user")
                .amount(BigDecimal.valueOf(100.00))
                .type(TransactionType.EXPENSE)
                .category(category)
                .description("Test transaction")
                .build();

        CreateTransactionCommand command = new CreateTransactionCommand(
                "Updated transaction",
                BigDecimal.valueOf(200.00),
                category.getId(),
                Instant.now(),
                TransactionType.EXPENSE,
                userId
        );

        when(transactionRepositoryPort.findById(transactionId)).thenReturn(Optional.of(otherUserTransaction));

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            transactionService.updateTransaction(transactionId, command);
        });

        assertEquals("Transaction not found with id: " + transactionId, exception.getMessage());
        verify(transactionRepositoryPort).findById(transactionId);
        verify(categoryRepositoryPort, never()).findById(any());
        verify(transactionRepositoryPort, never()).save(any());
    }

    @Test
    void updateTransaction_categoryNotFound() {
        CreateTransactionCommand command = new CreateTransactionCommand(
                "Updated transaction",
                BigDecimal.valueOf(200.00),
                category.getId(),
                Instant.now(),
                TransactionType.EXPENSE,
                userId
        );

        when(transactionRepositoryPort.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(categoryRepositoryPort.findById(category.getId())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            transactionService.updateTransaction(transactionId, command);
        });

        assertEquals("Category not found with id: " + category.getId(), exception.getMessage());
        verify(transactionRepositoryPort).findById(transactionId);
        verify(categoryRepositoryPort).findById(category.getId());
        verify(transactionRepositoryPort, never()).save(any());
    }

    @Test
    void getListTransaction_success() {
        GetTransactionCommand command = new GetTransactionCommand(
                0, 10, "occurredAt", "desc", userId, null, null, null, null, null, 0, 0
        );

        PageResult<Transaction> pageResult = PageResult.of(
                java.util.List.of(transaction), 0, 10, 1, 1, true
        );

        when(transactionRepositoryPort.findAllWithFilter(command)).thenReturn(pageResult);

        PageResult<TransactionResponse> response = transactionService.getListTransaction(command);

        verify(transactionRepositoryPort).findAllWithFilter(command);

        assertEquals(1, response.content().size());
        assertEquals(transaction.getId().toString(), response.content().get(0).id());
        assertEquals(0, response.page());
        assertEquals(10, response.size());
        assertEquals(1, response.totalElements());
        assertEquals(1, response.totalPages());
        assertTrue(response.last());
    }

}
