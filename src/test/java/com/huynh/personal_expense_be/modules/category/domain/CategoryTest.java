package com.huynh.personal_expense_be.modules.category.domain;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class CategoryTest {

    @Test
    void shouldCreateCategory() {
        // Given
        Category category = Category.builder()
                .id(UUID.randomUUID())
                .userId("user123")
                .parentId(null)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .isDeleted(null)
                .name("Food")
                .build();

        // When

        assertThat(category.getName()).isEqualTo("Food");
        assertThat(category.getUserId()).isEqualTo("user123");
        assertThat(category.getParentId()).isNull();
        assertThat(category.getCreatedAt()).isNotNull();
        assertThat(category.getUpdatedAt()).isNotNull();
        assertThat(category.getIsDeleted()).isNull();

    }

    @Test
    void shouldCreateSubCategory() {
        // Given
        Category parentCategory = Category.builder()
                .id(UUID.randomUUID())
                .userId("user123")
                .parentId(null)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .isDeleted(null)
                .name("Food")
                .build();

        Category subCategory = Category.builder()
                .id(UUID.randomUUID())
                .userId("user123")
                .parentId(parentCategory.getId())
                .parent(parentCategory)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .isDeleted(null)
                .name("Groceries")
                .build();

        // When
        assertThat(subCategory.getName()).isEqualTo("Groceries");
        assertThat(subCategory.getUserId()).isEqualTo("user123");
        assertThat(subCategory.getParentId()).isEqualTo(parentCategory.getId());
        assertThat(subCategory.getParent()).isEqualTo(parentCategory);
        assertThat(subCategory.getCreatedAt()).isNotNull();
        assertThat(subCategory.getUpdatedAt()).isNotNull();
        assertThat(subCategory.getIsDeleted()).isNull();
    }

}
