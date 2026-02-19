package com.huynh.personal_expense_be.modules.category.infrastructure.persistence;

import com.huynh.personal_expense_be.modules.category.application.port.out.CategoryRepositoryPort;
import com.huynh.personal_expense_be.modules.category.domain.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CategoryPersistenceAdapter implements CategoryRepositoryPort {

    @PersistenceContext
    private EntityManager entityManager;
    private final CategoryMapper categoryMapper;

    @Override
    public Category save(Category category) {
        CategoryJpaEntity entity = categoryMapper.toJpaEntity(category);
        CategoryJpaEntity saved = entityManager.merge(entity);
        return categoryMapper.toDomain(saved);
    }

    @Override
    public Optional<Category> findById(UUID id) {
        CategoryJpaEntity entity = entityManager.find(CategoryJpaEntity.class, id);
        return Optional.ofNullable(entity).map(categoryMapper::toDomain);
    }

    @Override
    public List<Category> findAll() {
        return entityManager
                .createQuery("SELECT c FROM CategoryJpaEntity c", CategoryJpaEntity.class)
                .getResultList()
                .stream()
                .map(categoryMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        CategoryJpaEntity entity = entityManager.find(CategoryJpaEntity.class, id);
        if (entity != null) {
            entityManager.remove(entity);
        }
    }

    @Override
    public boolean existsById(UUID id) {
        Long count = entityManager
                .createQuery("SELECT COUNT(c) FROM CategoryJpaEntity c WHERE c.id = :id", Long.class)
                .setParameter("id", id)
                .getSingleResult();
        return count > 0;
    }

    @Override
    public boolean existsByNameAndUserId(String name, String userId) {
        Long count = entityManager
                .createQuery(
                        "SELECT COUNT(c) FROM CategoryJpaEntity c WHERE c.name = :name AND c.userId = :userId",
                        Long.class)
                .setParameter("name", name)
                .setParameter("userId", userId)
                .getSingleResult();
        return count > 0;
    }
}
