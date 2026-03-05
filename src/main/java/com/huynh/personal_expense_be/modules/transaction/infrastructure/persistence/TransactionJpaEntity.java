package com.huynh.personal_expense_be.modules.transaction.infrastructure.persistence;

import com.huynh.personal_expense_be.modules.category.infrastructure.persistence.CategoryJpaEntity;
import com.huynh.personal_expense_be.modules.transaction.domain.TransactionType;
import com.huynh.personal_expense_be.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data @EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "transactions")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TransactionJpaEntity extends BaseEntity {



    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false,name = "amount", columnDefinition = "DECIMAL(19, 4)")
    private BigDecimal amount;

    @Column(nullable = false,name = "user_id", columnDefinition = "VARCHAR(255)")
    private String userId;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryJpaEntity category;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;



    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, columnDefinition = "VARCHAR(255)")
    private TransactionType type;


}
