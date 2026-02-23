package com.huynh.personal_expense_be.modules.category.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter @Builder(toBuilder = true)
@AllArgsConstructor @NoArgsConstructor
public class Category {

    private UUID id;

    private String name;

    private String userId;

    private UUID parentId;

    private Category parent;

    private Instant createdAt;

    private Instant updatedAt;

    private Instant isDeleted;


}
