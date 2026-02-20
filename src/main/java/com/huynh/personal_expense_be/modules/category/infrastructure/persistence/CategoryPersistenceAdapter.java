package com.huynh.personal_expense_be.modules.category.infrastructure.persistence;

import com.huynh.personal_expense_be.modules.category.application.port.out.CategoryRepositoryPort;
import com.huynh.personal_expense_be.modules.category.domain.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
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
        CategoryJpaEntity entity = entityManager.createQuery(
                        "SELECT c FROM CategoryJpaEntity c WHERE c.id = :id AND c.isDeleted IS NULL",
                        CategoryJpaEntity.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElse(null);

        return Optional.ofNullable(entity).map(categoryMapper::toDomain);
    }

    @Override
    public List<Category> findAll() {
        return entityManager
                .createQuery("SELECT c FROM CategoryJpaEntity c WHERE c.isDeleted IS NOT NULL ", CategoryJpaEntity.class)
                .getResultList()
                .stream()
                .map(categoryMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        Optional<CategoryJpaEntity> entity = entityManager.createQuery(
                        "SELECT c FROM CategoryJpaEntity c WHERE c.id = :id AND c.isDeleted IS NULL",
                        CategoryJpaEntity.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();

        if (entity.isPresent()) {
            entity.get().setIsDeleted(Instant.now());
            entityManager.merge(entity.get());
        }
    }

    @Override
    public boolean existsById(UUID id) {
        Long count = entityManager
                .createQuery("SELECT COUNT(c) FROM CategoryJpaEntity c WHERE c.id = :id AND c.isDeleted IS NOT NULL " , Long.class)
                .setParameter("id", id)
                .getSingleResult();
        return count > 0;
    }

    @Override
    public boolean existsByNameAndUserId(String name, String userId) {
        Long count = entityManager
                .createQuery(
                        "SELECT COUNT(c) FROM CategoryJpaEntity c WHERE c.name = :name AND c.userId = :userId AND c.isDeleted IS NULL",
                        Long.class)
                .setParameter("name", name)
                .setParameter("userId", userId)
                .getSingleResult();
        return count > 0;
    }
}
