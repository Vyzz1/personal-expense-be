package com.huynh.personal_expense_be.modules.category.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class CategoryAnalysisTest {

    @Test
    void shouldAnalyzeCategory() {
        // Given

        CategoryAnalysis categoryAnalysis = CategoryAnalysis.builder()
                .id(UUID.randomUUID())
                .year(2026)
                .month(6)
                .name("Food")
                .totalAmount(BigDecimal.valueOf(1000))
                .transactionCount(5L)
                .build();

        // When

        assertThat(categoryAnalysis.getId()).isNotNull();
        assertThat(categoryAnalysis.getYear()).isEqualTo(2026);
        assertThat(categoryAnalysis.getMonth()).isEqualTo(6);
        assertThat(categoryAnalysis.getName()).isEqualTo("Food");
        assertThat(categoryAnalysis.getTotalAmount()).isEqualTo(BigDecimal.valueOf(1000));
        assertThat(categoryAnalysis.getTransactionCount()).isEqualTo(5L);

    }

}
