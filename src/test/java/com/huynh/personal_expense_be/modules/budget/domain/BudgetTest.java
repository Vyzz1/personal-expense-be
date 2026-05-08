package com.huynh.personal_expense_be.modules.budget.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.huynh.personal_expense_be.modules.category.domain.Category;

public class BudgetTest {
    
    @Test
    void shouldCreateBudgetWithCorrectValues() {
        // Given
        String userId = "user123";
        UUID categoryId = UUID.randomUUID();
        Category category = Category.builder()
                .id(categoryId)
                .userId(userId)
                .name("Food")
                .build();
        String period = "2024-06";
        Budget budget = Budget.builder()
                .userId(userId)
                .category(category)
                .period(YearMonth.parse(period))
                .limitAmount(BigDecimal.ZERO)
                .spentAmount(BigDecimal.ZERO)
                .build();

        // Then
        assertThat(budget.getUserId()).isEqualTo(userId);
        assertThat(budget.getCategory()).isEqualTo(category);
        assertThat(budget.getPeriod()).isEqualTo(YearMonth.parse(period));
        assertThat(budget.getLimitAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(budget.getSpentAmount()).isEqualTo(BigDecimal.ZERO);

    }

    @Test
    void shouldUpdateBudgetWithNewValues() {
        // Given
        Budget budget = Budget.builder()
                .name("June Budget")
                .limitAmount(BigDecimal.valueOf(500))
                .status(BudgetStatus.ACTIVE)
                .build();

        // When
        Budget updatedBudget = budget.update("Updated June Budget", BigDecimal.valueOf(600), BudgetStatus.EXPIRED);

        // Then
        assertThat(updatedBudget.getName()).isEqualTo("Updated June Budget");
        assertThat(updatedBudget.getLimitAmount()).isEqualTo(BigDecimal.valueOf(600));
        assertThat(updatedBudget.getStatus()).isEqualTo(BudgetStatus.EXPIRED);
    }

    @Test
    void shouldAddSpentAmountToBudget() {
        // Given
        Budget budget = Budget.builder()
                .spentAmount(BigDecimal.valueOf(100))
                .build();

        // When
        Budget updatedBudget = budget.addSpentAmount(BigDecimal.valueOf(50));

        // Then
        assertThat(updatedBudget.getSpentAmount()).isEqualTo(BigDecimal.valueOf(150));
    }

    @Test
    void shouldSubtractSpentAmountFromBudget() {
        // Given
        Budget budget = Budget.builder()
                .spentAmount(BigDecimal.valueOf(100))
                .build();

        // When
        Budget updatedBudget = budget.subtractSpentAmount(BigDecimal.valueOf(30));

        // Then
        assertThat(updatedBudget.getSpentAmount()).isEqualTo(BigDecimal.valueOf(70));
    }

}
