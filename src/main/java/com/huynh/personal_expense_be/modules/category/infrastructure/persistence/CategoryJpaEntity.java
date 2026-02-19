package com.huynh.personal_expense_be.modules.category.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "categories")
@NoArgsConstructor @AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CategoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false,name = "name",columnDefinition = "VARCHAR(255)")
    private String name;

    @Column(nullable = false,name = "user_id",columnDefinition = "VARCHAR(255)")
    private String userId;

    @Column(name = "parent_id", columnDefinition = "UUID")
    private UUID parentId;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "is_deleted", nullable = true)
    private Instant isDeleted;
}
