package com.huynh.personal_expense_be.modules.category.application.port.out;

import com.huynh.personal_expense_be.modules.category.domain.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepositoryPort {

    Category save(Category category);

    Optional<Category> findById(UUID id);

    List<Category> findAll();

    void deleteById(UUID id);

    boolean existsById(UUID id);

    boolean existsByNameAndUserId(String name, String userId);
}
