package com.huynh.personal_expense_be.modules.expense.infrastructure.persistence;

import com.huynh.personal_expense_be.modules.category.infrastructure.persistence.CategoryJpaEntity;
import com.huynh.personal_expense_be.modules.transaction.infrastructure.persistence.TransactionJpaEntity;
import com.huynh.personal_expense_be.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;

@Data @EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Entity
@Table(name = "monthly_expenses",indexes = {
        @Index(name = "idx_monthly_expenses_transaction_id", columnList = "transaction_id"),
        @Index(name = "idx_monthly_expenses_category_id", columnList = "category_id")
},
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_monthly_expenses_user_month_year", columnNames = {"user_id", "month", "year"})
        }
)
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MonthlyExpenseJpaEntity extends BaseEntity {


    @Column(nullable = false,name = "user_id",columnDefinition = "VARCHAR(255)")
    private String userId;

    @Column(nullable = false,name = "month",columnDefinition = "SMALLINT")
    private int month;

    @Column(nullable = false,name = "year",columnDefinition = "SMALLINT")
    private int year;

    @Column(nullable = false,name = "total_amount",columnDefinition = "DECIMAL(19, 4)")
    private BigDecimal totalAmount;

    @Column(name = "previous_total_amount", columnDefinition = "DECIMAL(19, 4)")
    private BigDecimal previousTotalAmount;

    @Column(name = "change_percentage", columnDefinition = "DECIMAL(5, 2)")
    private BigDecimal changePercentage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private TransactionJpaEntity transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryJpaEntity category;



}
