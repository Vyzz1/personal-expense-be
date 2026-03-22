package com.huynh.personal_expense_be.modules.category.application.service;

import com.huynh.personal_expense_be.modules.category.application.dto.CategoryAnalysisResponse;
import com.huynh.personal_expense_be.modules.category.application.dto.CategoryResponse;
import com.huynh.personal_expense_be.modules.category.application.port.out.CategoryRepositoryPort;
import com.huynh.personal_expense_be.modules.category.domain.Category;
import com.huynh.personal_expense_be.modules.category.domain.CategoryAnalysis;
import com.huynh.personal_expense_be.shared.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class QueryCategoryServiceTest {

    @Mock
    private CategoryRepositoryPort categoryRepositoryPort;

    @InjectMocks
    private QueryCategoryService queryCategoryService;

    @Test
    void getCategoryById_success() {
        UUID categoryId = UUID.randomUUID();
        var category = Category.builder()
                .id(categoryId)
                .name("Food")
                .userId("user-1")
                .parentId(null)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(categoryRepositoryPort.findById(categoryId)).thenReturn(Optional.of(category));

        CategoryResponse result = queryCategoryService.getCategoryById(categoryId);

        assertEquals("Food", result.name());
        assertEquals("user-1", result.userId());
        assertEquals(categoryId, result.id());
        assertNull(result.parent());
        verify(categoryRepositoryPort).findById(categoryId);
    }

    @Test
    void getCategoryById_throwsNotFoundException() {
        UUID categoryId = UUID.randomUUID();

        when(categoryRepositoryPort.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> queryCategoryService.getCategoryById(categoryId));
        verify(categoryRepositoryPort).findById(categoryId);
    }

    @Test
    void getAllCategories_returnsEmptyList() {
        when(categoryRepositoryPort.findAll()).thenReturn(List.of());

        List<CategoryResponse> result = queryCategoryService.getAllCategories();

        assertTrue(result.isEmpty());
        verify(categoryRepositoryPort).findAll();
    }

    @Test
    void getAllCategories_mapsFromDomainList() {
        var categories = List.of(
                Category.builder()
                        .id(UUID.randomUUID())
                        .name("Food")
                        .userId("user-1")
                        .createdAt(Instant.now())
                        .build(),
                Category.builder()
                        .id(UUID.randomUUID())
                        .name("Transport")
                        .userId("user-1")
                        .createdAt(Instant.now())
                        .build());

        when(categoryRepositoryPort.findAll()).thenReturn(categories);

        List<CategoryResponse> result = queryCategoryService.getAllCategories();

        assertEquals(2, result.size());
        assertEquals("Food", result.get(0).name());
        assertEquals("Transport", result.get(1).name());
        verify(categoryRepositoryPort).findAll();
    }

    @Test
    void getCategoryAnalysis_returnsEmptyList() {
        when(categoryRepositoryPort.getCategoryAnalysis("user-1")).thenReturn(List.of());

        List<CategoryAnalysisResponse> result = queryCategoryService.getCategoryAnalysis("user-1");

        assertTrue(result.isEmpty());
        verify(categoryRepositoryPort).getCategoryAnalysis("user-1");
    }

    @Test
    void getCategoryAnalysis_mapsFromDomainList() {
        var analyses = List.of(
                CategoryAnalysis.builder()
                        .id(UUID.randomUUID())
                        .month(3)
                        .year(2026)
                        .name("Food")
                        .totalAmount(new BigDecimal("150.50"))
                        .transactionCount(5L)
                        .build(),
                CategoryAnalysis.builder()
                        .id(UUID.randomUUID())
                        .month(3)
                        .year(2026)
                        .name("Transport")
                        .totalAmount(new BigDecimal("75.00"))
                        .transactionCount(3L)
                        .build());

        when(categoryRepositoryPort.getCategoryAnalysis("user-1")).thenReturn(analyses);

        List<CategoryAnalysisResponse> result = queryCategoryService.getCategoryAnalysis("user-1");

        assertEquals(2, result.size());
        assertEquals(3, result.get(0).month());
        assertEquals(2026, result.get(0).year());
        assertEquals("Food", result.get(0).name());
        assertEquals(new BigDecimal("150.50"), result.get(0).totalAmount());
        assertEquals(5L, result.get(0).transactionCount());
        assertEquals("Transport", result.get(1).name());
        verify(categoryRepositoryPort).getCategoryAnalysis("user-1");
    }

}
