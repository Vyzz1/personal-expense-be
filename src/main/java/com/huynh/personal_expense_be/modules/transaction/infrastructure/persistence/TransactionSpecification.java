package com.huynh.personal_expense_be.modules.transaction.infrastructure.persistence;

import com.huynh.personal_expense_be.modules.transaction.domain.TransactionType;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

public class TransactionSpecification {

    private TransactionSpecification() {}

    public static Specification<TransactionJpaEntity> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("isDeleted"));
    }

    public static Specification<TransactionJpaEntity> byUserId(String userId) {
        return (root, query, cb) -> cb.equal(root.get("userId"), userId);
    }

    // Fetch category only on data queries — count queries fail with fetch joins
    public static Specification<TransactionJpaEntity> withCategory() {
        return (root, query, cb) -> {
            if (!Long.class.equals(query.getResultType())) {
                root.fetch("category", JoinType.INNER);
            }
            return cb.conjunction();
        };
    }

    public static Specification<TransactionJpaEntity> descriptionContains(String description) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%");
    }

    public static Specification<TransactionJpaEntity> inCategories(List<UUID> categoryIds) {
        return (root, query, cb) -> root.get("category").get("id").in(categoryIds);
    }

    public static Specification<TransactionJpaEntity> byTypes(List<TransactionType> types) {
        return (root, query, cb) -> root.get("type").in(types);
    }

    public static Specification<TransactionJpaEntity> fromDate(Instant fromDate) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("occurredAt"), fromDate);
    }

    public static Specification<TransactionJpaEntity> toDate(Instant toDate) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("occurredAt"), toDate);
    }

    // Date range approach — more portable and index-friendly than MONTH()/YEAR() functions
    public static Specification<TransactionJpaEntity> byMonthYear(int month, int year) {
        ZoneId zone = ZoneId.systemDefault();
        YearMonth ym = YearMonth.of(year, month);
        Instant start = ym.atDay(1).atStartOfDay(zone).toInstant();
        Instant end = ym.atEndOfMonth().atTime(LocalTime.MAX).atZone(zone).toInstant();
        return (root, query, cb) -> cb.between(root.get("occurredAt"), start, end);
    }

    public static Specification<TransactionJpaEntity> minAmount(BigDecimal min) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("amount"), min);
    }

    public static Specification<TransactionJpaEntity> maxAmount(BigDecimal max) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("amount"), max);
    }
}
