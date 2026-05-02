package com.huynh.personal_expense_be.modules.budget.infrastructure.persistence;

import com.huynh.personal_expense_be.modules.budget.domain.BudgetStatus;
import com.huynh.personal_expense_be.modules.category.infrastructure.persistence.CategoryJpaEntity;
import com.huynh.personal_expense_be.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.YearMonth;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "budgets", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "period", "category_id"}, name = "uk_user_period_category")
})
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class BudgetJpaEntity extends BaseEntity {

    @Column(name = "name", nullable = false, columnDefinition = "VARCHAR(255)")
    private String name;

    @Column(name = "user_id", nullable = false, columnDefinition = "VARCHAR(255)")
    private String userId;

    @Column(name = "limit_amount", nullable = false, columnDefinition = "DECIMAL(19, 4)")
    private BigDecimal limitAmount;

    @Column(name = "spent_amount", nullable = false, columnDefinition = "DECIMAL(19, 4)")
    private BigDecimal spentAmount;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = true)
    private CategoryJpaEntity category;

    @Column(name = "budget_status", nullable = false, columnDefinition = "VARCHAR(255)")
    @Enumerated(EnumType.STRING)
    private BudgetStatus status;

    @Column(name = "period", nullable = false, columnDefinition = "VARCHAR(7)")
    private YearMonth period;

}
