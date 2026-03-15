package com.huynh.personal_expense_be.modules.category.infrastructure.persistence;

import com.huynh.personal_expense_be.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@SuperBuilder(toBuilder = true)
@Entity
@Table(name = "categories")
@NoArgsConstructor @AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class CategoryJpaEntity extends BaseEntity {



    @Column(nullable = false,name = "name",columnDefinition = "VARCHAR(255)")
    private String name;

    @Column(nullable = false,name = "user_id",columnDefinition = "VARCHAR(255)")
    private String userId;

    @Column(name = "parent_id", columnDefinition = "UUID")
    private UUID parentId;


}
