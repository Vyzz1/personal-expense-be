package com.huynh.personal_expense_be.modules.budget.infrastructure.persistence;

import com.huynh.personal_expense_be.modules.budget.domain.Budget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class BudgetRepositoryAdapterTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private BudgetMapper budgetMapper;

    @InjectMocks
    private BudgetRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @SuppressWarnings("unchecked")
    private TypedQuery<BudgetJpaEntity> mockBudgetQuery() {
        return (TypedQuery<BudgetJpaEntity>) mock(TypedQuery.class);
    }

    @SuppressWarnings("unchecked")
    private TypedQuery<Long> mockLongQuery() {
        return (TypedQuery<Long>) mock(TypedQuery.class);
    }

    @Test
    void testSave_NewEntity() {
        Budget budget = Budget.builder().id(null).build();
        BudgetJpaEntity entity = new BudgetJpaEntity();

        when(budgetMapper.toEntity(budget)).thenReturn(entity);
        when(budgetMapper.toDomain(entity)).thenReturn(budget);

        Budget result = adapter.save(budget);

        verify(entityManager).persist(entity);
        assertEquals(budget, result);
    }

    @Test
    void testSave_ExistingEntity() {
        Budget budget = Budget.builder().id(UUID.randomUUID()).build();
        BudgetJpaEntity entity = new BudgetJpaEntity();
        entity.setId(budget.getId());

        when(budgetMapper.toEntity(budget)).thenReturn(entity);
        when(entityManager.merge(entity)).thenReturn(entity);
        when(budgetMapper.toDomain(entity)).thenReturn(budget);

        Budget result = adapter.save(budget);

        verify(entityManager).merge(entity);
        assertEquals(budget, result);
    }

    @Test
    void testFindById_Found() {
        UUID id = UUID.randomUUID();
        String userId = "user1";
        BudgetJpaEntity entity = new BudgetJpaEntity();
        Budget budget = Budget.builder().id(id).build();

        TypedQuery<BudgetJpaEntity> query = mockBudgetQuery();
        when(entityManager.createQuery(anyString(), eq(BudgetJpaEntity.class))).thenReturn(query);
        when(query.setParameter("userId", userId)).thenReturn(query);
        when(query.setParameter("id", id)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(entity);
        when(budgetMapper.toDomain(entity)).thenReturn(budget);

        Optional<Budget> result = adapter.findById(userId, id);

        assertTrue(result.isPresent());
        assertEquals(budget, result.get());
    }

    @Test
    void testFindById_NotFound() {
        UUID id = UUID.randomUUID();
        String userId = "user1";

        TypedQuery<BudgetJpaEntity> query = mockBudgetQuery();
        when(entityManager.createQuery(anyString(), eq(BudgetJpaEntity.class))).thenReturn(query);
        when(query.setParameter("userId", userId)).thenReturn(query);
        when(query.setParameter("id", id)).thenReturn(query);
        when(query.getSingleResult()).thenThrow(new NoResultException());

        Optional<Budget> result = adapter.findById(userId, id);

        assertFalse(result.isPresent());
    }

    @Test
    void testDeleteById_Found() {
        UUID id = UUID.randomUUID();
        BudgetJpaEntity entity = new BudgetJpaEntity();

        when(entityManager.find(BudgetJpaEntity.class, id)).thenReturn(entity);

        adapter.deleteById(id);

        verify(entityManager).remove(entity);
    }

    @Test
    void testDeleteById_NotFound() {
        UUID id = UUID.randomUUID();

        when(entityManager.find(BudgetJpaEntity.class, id)).thenReturn(null);

        adapter.deleteById(id);

        verify(entityManager, never()).remove(any());
    }

    @Test
    void testFindByUserIdAndPeriod() {
        String userId = "user1";
        String period = "2026-05";
        BudgetJpaEntity entity = new BudgetJpaEntity();
        Budget budget = Budget.builder().build();

        TypedQuery<BudgetJpaEntity> query = mockBudgetQuery();
        when(entityManager.createQuery(anyString(), eq(BudgetJpaEntity.class))).thenReturn(query);
        when(query.setParameter("userId", userId)).thenReturn(query);
        when(query.setParameter("period", period)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(entity));
        when(budgetMapper.toDomain(entity)).thenReturn(budget);

        List<Budget> result = adapter.findByUserIdAndPeriod(userId, period);

        assertEquals(1, result.size());
        assertEquals(budget, result.get(0));
    }

    @Test
    void testExistsByUserId() {
        String userId = "user1";

        TypedQuery<Long> query = mockLongQuery();
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(query);
        when(query.setParameter("userId", userId)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(1L);

        boolean exists = adapter.existsByUserId(userId);

        assertTrue(exists);
    }

    @Test
    void testFindByUserId() {
        String userId = "user1";
        BudgetJpaEntity entity = new BudgetJpaEntity();
        Budget budget = Budget.builder().build();

        TypedQuery<BudgetJpaEntity> query = mockBudgetQuery();
        when(entityManager.createQuery(anyString(), eq(BudgetJpaEntity.class))).thenReturn(query);
        when(query.setParameter("userId", userId)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(entity));
        when(budgetMapper.toDomain(entity)).thenReturn(budget);

        List<Budget> result = adapter.findByUserId(userId);

        assertEquals(1, result.size());
        assertEquals(budget, result.get(0));
    }

    @Test
    void testExistsOverallByUserId() {
        String userId = "user1";

        TypedQuery<Long> query = mockLongQuery();
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(query);
        when(query.setParameter("userId", userId)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(1L);

        boolean exists = adapter.existsOverallByUserId(userId);

        assertTrue(exists);
    }

    @Test
    void testExistsByCategoryIdAndUserId() {
        UUID categoryId = UUID.randomUUID();
        String userId = "user1";

        TypedQuery<Long> query = mockLongQuery();
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(query);
        when(query.setParameter("categoryId", categoryId)).thenReturn(query);
        when(query.setParameter("userId", userId)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(1L);

        boolean exists = adapter.existsByCategoryIdAndUserId(categoryId, userId);

        assertTrue(exists);
    }
}
