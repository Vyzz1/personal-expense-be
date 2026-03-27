package com.huynh.personal_expense_be.modules.transaction.domain;

import com.huynh.personal_expense_be.modules.category.domain.Category;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import static org.assertj.core.api.Assertions.assertThat;

public class TransactionTest {

    @Test
    void shouldCreateTransaction() {
        // Given

        Category category = Category.builder()
                .id(null)
                .name("Food")
                .userId("user123")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Transaction transaction = Transaction.builder()
                .id(null)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .isDeleted(null)
                .category(category)
                .description("Grocery shopping")
                .userId("user123")
                .amount(BigDecimal.valueOf(50.00))
                .type(TransactionType.EXPENSE)
                .build();

        // When
        assertThat(transaction.getDescription()).isEqualTo("Grocery shopping");
        assertThat(transaction.getAmount()).isEqualTo(BigDecimal.valueOf(50.00));
        assertThat(transaction.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(transaction.getCategory()).isEqualTo(category);
    }
}
