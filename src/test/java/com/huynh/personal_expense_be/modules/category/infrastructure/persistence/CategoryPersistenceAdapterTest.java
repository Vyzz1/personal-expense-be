package com.huynh.personal_expense_be.modules.category.infrastructure.persistence;

import com.huynh.personal_expense_be.modules.category.domain.Category;
import com.huynh.personal_expense_be.modules.category.domain.CategoryAnalysis;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryPersistenceAdapterTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private TypedQuery<CategoryJpaEntity> typedQuery;

    @Mock
    private TypedQuery<Long> countQuery;

    @Mock
    private Query nativeQuery;

    @InjectMocks
    private CategoryPersistenceAdapter adapter;

    private UUID categoryId;
    private Category category;
    private CategoryJpaEntity categoryJpaEntity;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();

        category = Category.builder()
                .id(categoryId)
                .name("Test Category")
                .userId("user1")
                .build();

        categoryJpaEntity = new CategoryJpaEntity();
        categoryJpaEntity.setId(categoryId);
        categoryJpaEntity.setName("Test Category");
        categoryJpaEntity.setUserId("user1");

        ReflectionTestUtils.setField(adapter, "entityManager", entityManager);
    }

    @Test
    void save_ShouldMergeEntityAndReturnDomain() {
        when(categoryMapper.toJpaEntity(category)).thenReturn(categoryJpaEntity);
        when(entityManager.merge(categoryJpaEntity)).thenReturn(categoryJpaEntity);
        when(categoryMapper.toDomain(categoryJpaEntity, null)).thenReturn(category);

        Category result = adapter.save(category);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(categoryId);
        verify(entityManager).merge(categoryJpaEntity);
    }

    @Test
    void findById_ShouldReturnCategory_WhenFound() {
        when(entityManager.createQuery(anyString(), eq(CategoryJpaEntity.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter("id", categoryId)).thenReturn(typedQuery);
        when(typedQuery.getResultStream()).thenReturn(Stream.of(categoryJpaEntity));
        when(categoryMapper.toDomain(categoryJpaEntity, null)).thenReturn(category);

        Optional<Category> result = adapter.findById(categoryId);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(categoryId);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotFound() {
        when(entityManager.createQuery(anyString(), eq(CategoryJpaEntity.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter("id", categoryId)).thenReturn(typedQuery);
        when(typedQuery.getResultStream()).thenReturn(Stream.empty());

        Optional<Category> result = adapter.findById(categoryId);

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_ShouldReturnCategoryList() {
        when(entityManager.createQuery(anyString(), eq(CategoryJpaEntity.class))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of(categoryJpaEntity));
        when(categoryMapper.toDomain(categoryJpaEntity, null)).thenReturn(category);

        List<Category> result = adapter.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(categoryId);
    }

    @Test
    void deleteById_ShouldSetIsDeleted_WhenFound() {
        when(entityManager.createQuery(anyString(), eq(CategoryJpaEntity.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter("id", categoryId)).thenReturn(typedQuery);
        when(typedQuery.getResultStream()).thenReturn(Stream.of(categoryJpaEntity));

        adapter.deleteById(categoryId);

        assertThat(categoryJpaEntity.getIsDeleted()).isNotNull();
        verify(entityManager).merge(categoryJpaEntity);
    }

    @Test
    void existsById_ShouldReturnTrue_WhenCountIsGreaterThanZero() {
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter("id", categoryId)).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(1L);

        boolean result = adapter.existsById(categoryId);

        assertThat(result).isTrue();
    }

    @Test
    void existsByNameAndUserId_ShouldReturnCategory_WhenFound() {
        when(entityManager.createQuery(anyString(), eq(CategoryJpaEntity.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter("name", "Test Category")).thenReturn(typedQuery);
        when(typedQuery.setParameter("userId", "user1")).thenReturn(typedQuery);
        when(typedQuery.getResultStream()).thenReturn(Stream.of(categoryJpaEntity));
        when(categoryMapper.toDomain(categoryJpaEntity)).thenReturn(category);

        Optional<Category> result = adapter.existsByNameAndUserId("Test Category", "user1");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Test Category");
    }

    @Test
    void getCategoryAnalysis_ShouldReturnAnalysisList() {
        Object[] row = new Object[] { categoryId.toString(), "Test Category", BigDecimal.TEN, 5L, 10, 2023 };
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter("userId", "user1")).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(List.<Object[]>of(row));

        List<CategoryAnalysis> result = adapter.getCategoryAnalysis("user1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(categoryId);
        assertThat(result.get(0).getName()).isEqualTo("Test Category");
        assertThat(result.get(0).getTotalAmount()).isEqualTo(BigDecimal.TEN);
    }
}
