package com.huynh.personal_expense_be.modules.transaction.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.huynh.personal_expense_be.modules.transaction.domain.Transaction;
import com.huynh.personal_expense_be.modules.transaction.domain.TransactionType;
import com.huynh.personal_expense_be.modules.transaction.application.dto.GetTransactionCommand;
import com.huynh.personal_expense_be.modules.transaction.application.dto.PageResult;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
public class TransactionPersistenceAdapterTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private TypedQuery<TransactionJpaEntity> typedQuery;

    @Mock
    private TypedQuery<Long> countQuery;

    @Mock
    private Query nativeQuery;

    @InjectMocks
    private TransactionPersistenceAdapter adapter;

    private String userId;

    private UUID transactionId;

    private TransactionJpaEntity transactionJpaEntity;

    private Transaction transaction;

    @BeforeEach
    void setUp() {
        userId = "user1";
        transactionId = UUID.randomUUID();

        transactionJpaEntity = TransactionJpaEntity.builder()
                .id(transactionId)
                .userId(userId)
                .description("Test Transaction")
                .amount(new java.math.BigDecimal("100.00"))
                .type(TransactionType.EXPENSE)
                .occurredAt(java.time.Instant.now())
                .createdAt(java.time.Instant.now())
                .updatedAt(java.time.Instant.now())
                .build();

        transaction = Transaction.builder()
                .id(transactionId)
                .userId(userId)
                .description("Test Transaction")
                .amount(new java.math.BigDecimal("100.00"))
                .type(TransactionType.EXPENSE)
                .occurredAt(java.time.Instant.now())
                .createdAt(java.time.Instant.now())
                .updatedAt(java.time.Instant.now())
                .build();

        ReflectionTestUtils.setField(adapter, "entityManager", entityManager);
    }

    @Test
    void save_shouldMergeAndReturnDomain() {
        // Given
        when(transactionMapper.toJpaEntity(transaction)).thenReturn(transactionJpaEntity);
        when(entityManager.merge(transactionJpaEntity)).thenReturn(transactionJpaEntity);
        when(transactionMapper.toDomain(transactionJpaEntity)).thenReturn(transaction);

        // When
        var result = adapter.save(transaction);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(transactionId);
        verify(entityManager).merge(transactionJpaEntity);
    }

    @Test
    void findById_shouldReturnTransaction_whenFound() {
        // Given
        when(entityManager.createQuery(anyString(), eq(TransactionJpaEntity.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter("id", transactionId)).thenReturn(typedQuery);
        when(typedQuery.getResultStream()).thenReturn(Stream.of(transactionJpaEntity));
        when(transactionMapper.toDomain(transactionJpaEntity)).thenReturn(transaction);

        // When
        var result = adapter.findById(transactionId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(transactionId);
    }

    @Test
    void findById_shouldReturnEmpty_whenNotFound() {
        // Given
        when(entityManager.createQuery(anyString(), eq(TransactionJpaEntity.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter("id", transactionId)).thenReturn(typedQuery);
        when(typedQuery.getResultStream()).thenReturn(Stream.empty());

        // When
        var result = adapter.findById(transactionId);

        // Then
        assertThat(result).isEmpty();

    }

    @Test
    void deleteById_shouldExecuteUpdate() {
        // Given

        when(entityManager.createQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(eq("id"), eq(transactionId))).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(eq("now"), any(Instant.class))).thenReturn(nativeQuery);
        when(nativeQuery.executeUpdate()).thenReturn(1);

        // When
        adapter.deleteById(transactionId);

        // Then
        verify(entityManager).createQuery(anyString());
        verify(nativeQuery).setParameter("id", transactionId);
        verify(nativeQuery).setParameter(eq("now"), any(Instant.class));
        verify(nativeQuery).executeUpdate();
    }

    @Test
    void findByUserId_shouldReturnTransactions_whenFound() {
        // Given
        when(entityManager.createQuery(anyString(), eq(TransactionJpaEntity.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter("userId", userId)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(java.util.List.of(transactionJpaEntity));
        when(transactionMapper.toDomain(transactionJpaEntity)).thenReturn(transaction);

        // When
        var result = adapter.findByUserId(userId);

        // Then
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(transactionId);
    }

    @Test
    void findAllWithFilter_shouldReturnPageResult() {
        // Given
        GetTransactionCommand command = new GetTransactionCommand(
                0, 10, "occurredAt", "desc", userId, "Test",
                java.util.List.of(UUID.randomUUID()), "EXPENSE", "2023-01-01T00:00:00Z", "2023-12-31T23:59:59Z",
                3, 2023);

        when(entityManager.createQuery(anyString(), eq(TransactionJpaEntity.class))).thenReturn(typedQuery);
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);

        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);

        when(countQuery.getSingleResult()).thenReturn(1L);
        when(typedQuery.setFirstResult(anyInt())).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(anyInt())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(java.util.List.of(transactionJpaEntity));

        when(transactionMapper.toDomain(transactionJpaEntity)).thenReturn(transaction);

        // When
        PageResult<Transaction> result = adapter.findAllWithFilter(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).getId()).isEqualTo(transactionId);
    }

    @Test
    void findAllWithFilter_shouldReturnPageResult_whenNoFiltersApplied() {
        // Given
        GetTransactionCommand command = new GetTransactionCommand(
                0, 0, null, null, userId, null,
                null, null, null, null,
                0, 0);

        when(entityManager.createQuery(anyString(), eq(TransactionJpaEntity.class))).thenReturn(typedQuery);
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);

        when(typedQuery.setParameter("userId", userId)).thenReturn(typedQuery);
        when(countQuery.setParameter("userId", userId)).thenReturn(countQuery);

        when(countQuery.getSingleResult()).thenReturn(1L);
        when(typedQuery.setFirstResult(anyInt())).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(anyInt())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(java.util.List.of(transactionJpaEntity));

        when(transactionMapper.toDomain(transactionJpaEntity)).thenReturn(transaction);

        // When
        PageResult<Transaction> result = adapter.findAllWithFilter(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).getId()).isEqualTo(transactionId);
    }

    @Test
    void findAllWithFilter_shouldReturnPageResult_whenFiltersAreEmptySkipped() {
        // Given
        GetTransactionCommand command = new GetTransactionCommand(
                0, -1, "", "", userId, "  ",
                java.util.List.of(), "  ", "  ", "  ",
                -1, -1);

        when(entityManager.createQuery(anyString(), eq(TransactionJpaEntity.class))).thenReturn(typedQuery);
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);

        when(typedQuery.setParameter("userId", userId)).thenReturn(typedQuery);
        when(countQuery.setParameter("userId", userId)).thenReturn(countQuery);

        when(countQuery.getSingleResult()).thenReturn(1L);
        when(typedQuery.setFirstResult(anyInt())).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(anyInt())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(java.util.List.of(transactionJpaEntity));

        when(transactionMapper.toDomain(transactionJpaEntity)).thenReturn(transaction);

        // When
        PageResult<Transaction> result = adapter.findAllWithFilter(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).getId()).isEqualTo(transactionId);
    }

}
