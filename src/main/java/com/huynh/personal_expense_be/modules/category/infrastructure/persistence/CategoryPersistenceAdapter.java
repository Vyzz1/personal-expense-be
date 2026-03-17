package com.huynh.personal_expense_be.modules.category.infrastructure.persistence;

import com.huynh.personal_expense_be.modules.category.application.port.out.CategoryRepositoryPort;
import com.huynh.personal_expense_be.modules.category.domain.Category;
import com.huynh.personal_expense_be.modules.category.domain.CategoryAnalysis;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
        Category parent = resolveParent(saved.getParentId());
        return categoryMapper.toDomain(saved, parent);
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

        if (entity == null) return Optional.empty();
        Category parent = resolveParent(entity.getParentId());
        return Optional.of(categoryMapper.toDomain(entity, parent));
    }

    @Override
    public List<Category> findAll() {
        List<CategoryJpaEntity> entities = entityManager
                .createQuery("SELECT c FROM CategoryJpaEntity c WHERE c.isDeleted IS NULL", CategoryJpaEntity.class)
                .getResultList();

        Map<UUID, CategoryJpaEntity> entityMap = entities.stream()
                .collect(Collectors.toMap(CategoryJpaEntity::getId, e -> e));

        return entities.stream()
                .map(e -> {
                    Category parent = null;
                    if (e.getParentId() != null) {
                        CategoryJpaEntity parentEntity = entityMap.get(e.getParentId());
                        if (parentEntity != null) {
                            parent = categoryMapper.toDomain(parentEntity);
                        }
                    }
                    return categoryMapper.toDomain(e, parent);
                })
                .toList();
    }

    private Category resolveParent(UUID parentId) {
        if (parentId == null) return null;
        CategoryJpaEntity parentEntity = entityManager.createQuery(
                        "SELECT c FROM CategoryJpaEntity c WHERE c.id = :id AND c.isDeleted IS NULL",
                        CategoryJpaEntity.class)
                .setParameter("id", parentId)
                .getResultStream()
                .findFirst()
                .orElse(null);
        return parentEntity != null ? categoryMapper.toDomain(parentEntity) : null;
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
                .createQuery("SELECT COUNT(c) FROM CategoryJpaEntity c WHERE c.id = :id AND c.isDeleted IS  NULL " , Long.class)
                .setParameter("id", id)
                .getSingleResult();
        return count > 0;
    }

    @Override
    public  Optional<Category> existsByNameAndUserId(String name, String userId) {
        CategoryJpaEntity category = entityManager
                .createQuery(
                        "SELECT c FROM CategoryJpaEntity c WHERE c.name = :name AND c.userId = :userId AND c.isDeleted IS NULL",
                        CategoryJpaEntity.class)
                .setParameter("name", name)
                .setParameter("userId", userId)
                .getResultStream()
                .findFirst()
                .orElse(null);

        return category != null ? Optional.of(categoryMapper.toDomain(category)) : Optional.empty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<CategoryAnalysis> getCategoryAnalysis(String userId) {
        List<Object[]> rows = entityManager.createNativeQuery("""
                SELECT
                    c.id::text,
                    c.name,
                    SUM(t.amount)                    AS total,
                    COUNT(t.id)                      AS transaction_count,
                    EXTRACT(MONTH FROM t.occurred_at) AS month,
                    EXTRACT(YEAR  FROM t.occurred_at) AS year
                FROM categories c
                JOIN transactions t ON c.id = t.category_id
                WHERE t.type = 'EXPENSE'
                  AND t.user_id = :userId
                  AND t.is_deleted IS NULL
                  AND t.occurred_at BETWEEN (NOW() - INTERVAL '3 MONTHS') AND NOW()
                GROUP BY c.id, c.name,
                         EXTRACT(MONTH FROM t.occurred_at),
                         EXTRACT(YEAR  FROM t.occurred_at)
                ORDER BY year, month, total DESC
                """)
                .setParameter("userId", userId)
                .getResultList();

        return rows.stream()
                .map(row -> new CategoryAnalysis(
                        UUID.fromString(row[0].toString()),
                        ((Number) row[4]).intValue(),
                        ((Number) row[5]).intValue(),
                        (String) row[1],
                        (BigDecimal) row[2],
                        (Long) row[3]
                ))
                .toList();
    }
}
