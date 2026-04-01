package com.huynh.personal_expense_be.modules.category.application.service;

import com.huynh.personal_expense_be.modules.category.application.dto.CategoryResponse;
import com.huynh.personal_expense_be.modules.category.application.dto.CreateCategoryCommand;
import com.huynh.personal_expense_be.modules.category.application.dto.UpdateCategoryCommand;
import com.huynh.personal_expense_be.modules.category.application.port.out.CategoryRepositoryPort;
import com.huynh.personal_expense_be.modules.category.domain.Category;
import com.huynh.personal_expense_be.shared.exception.DuplicateException;
import com.huynh.personal_expense_be.shared.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommandCategoryServiceTest {

    @Mock
    private CategoryRepositoryPort categoryRepositoryPort;

    @InjectMocks
    private CommandCategoryService commandCategoryService;

    @Test
    void createCategory_success() {
        var command = new CreateCategoryCommand("Food", "user-1", null);
        var savedCategory = Category.builder()
                .id(UUID.randomUUID())
                .name("Food")
                .userId("user-1")
                .parentId(null)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(categoryRepositoryPort.existsByNameAndUserId("Food", "user-1")).thenReturn(Optional.empty());
        when(categoryRepositoryPort.save(any(Category.class))).thenReturn(savedCategory);

        CategoryResponse result = commandCategoryService.createCategory(command);

        assertEquals("Food", result.name());
        assertEquals("user-1", result.userId());
        assertNull(result.parent());
        verify(categoryRepositoryPort).existsByNameAndUserId("Food", "user-1");
        verify(categoryRepositoryPort).save(any(Category.class));
    }

    @Test
    void createCategory_throwsDuplicateException() {
        var command = new CreateCategoryCommand("Food", "user-1", null);

        var existingCategory = Category.builder()
                .id(UUID.randomUUID())
                .name("Food")
                .userId("user-1")
                .parentId(null)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(categoryRepositoryPort.existsByNameAndUserId("Food", "user-1")).thenReturn(Optional.of(existingCategory));

        assertThrows(DuplicateException.class, () -> commandCategoryService.createCategory(command));

        verify(categoryRepositoryPort).existsByNameAndUserId("Food", "user-1");
    }

    @Test
    void updateCategory_success() {
        UUID categoryId = UUID.randomUUID();
        var command = new UpdateCategoryCommand("Food Updated", null);
        var existingCategory = Category.builder()
                .id(categoryId)
                .name("Food")
                .userId("user-1")
                .parentId(null)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        var updatedCategory = Category.builder()
                .id(categoryId)
                .name("Food Updated")
                .userId("user-1")
                .parentId(null)
                .createdAt(existingCategory.getCreatedAt())
                .updatedAt(Instant.now())
                .build();

        when(categoryRepositoryPort.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryRepositoryPort.save(any(Category.class))).thenReturn(updatedCategory);

        CategoryResponse result = commandCategoryService.updateCategory(categoryId, command);

        assertEquals("Food Updated", result.name());
        assertEquals("user-1", result.userId());
        verify(categoryRepositoryPort).findById(categoryId);
        verify(categoryRepositoryPort).save(any(Category.class));
    }

    @Test
    void updateCategory_throwsNotFoundException() {
        UUID categoryId = UUID.randomUUID();
        var command = new UpdateCategoryCommand("Food Updated", null);

        when(categoryRepositoryPort.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> commandCategoryService.updateCategory(categoryId, command));

        verify(categoryRepositoryPort).findById(categoryId);
    }

    @Test
    void deleteCategory_success() {
        UUID categoryId = UUID.randomUUID();

        when(categoryRepositoryPort.existsById(categoryId)).thenReturn(true);

        commandCategoryService.deleteCategory(categoryId);

        verify(categoryRepositoryPort).existsById(categoryId);
        verify(categoryRepositoryPort).deleteById(categoryId);
    }

    @Test
    void deleteCategory_throwsNotFoundException() {
        UUID categoryId = UUID.randomUUID();

        when(categoryRepositoryPort.existsById(categoryId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> commandCategoryService.deleteCategory(categoryId));

        verify(categoryRepositoryPort).existsById(categoryId);
    }

}
