package com.huynh.personal_expense_be.modules.expense.infrastructure.persistence;

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


}
