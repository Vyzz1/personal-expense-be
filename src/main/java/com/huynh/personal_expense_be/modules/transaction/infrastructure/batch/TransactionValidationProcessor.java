package com.huynh.personal_expense_be.modules.transaction.infrastructure.batch;

import com.huynh.personal_expense_be.modules.category.application.port.out.CategoryRepositoryPort;
import com.huynh.personal_expense_be.modules.category.domain.Category;
import com.huynh.personal_expense_be.modules.category.infrastructure.persistence.CategoryMapper;
import com.huynh.personal_expense_be.modules.transaction.application.dto.TransactionCsv;
import com.huynh.personal_expense_be.modules.transaction.domain.TransactionType;
import com.huynh.personal_expense_be.modules.transaction.infrastructure.persistence.TransactionJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionValidationProcessor implements ItemProcessor<TransactionCsv, TransactionJpaEntity> {
    private final CategoryRepositoryPort categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public TransactionJpaEntity process(TransactionCsv item) throws Exception {
        if (item.amount() == null || item.amount().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Invalid amount: {}", item.amount());
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (item.description() == null || item.description().isEmpty()) {
            log.error("Description cannot be empty");
            throw new IllegalArgumentException("Description cannot be empty");
        }
        if (item.date() == null || item.date().isEmpty()) {
            log.error("Date cannot be empty");
            throw new IllegalArgumentException("Date cannot be empty");
        }
        if (item.type() == null || item.type().isEmpty()) {
            log.error("Type cannot be empty");
            throw new IllegalArgumentException("Type cannot be empty");
        }
        if (item.category() == null || item.category().isEmpty()) {
            log.error("Category cannot be empty");
            throw new IllegalArgumentException("Category cannot be empty");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
        LocalDate localDate = LocalDate.parse(item.date(), formatter);

        Category category = categoryRepository.existsByNameAndUserId(item.category(), item.userId()).orElse(null);

        if (category == null) {
            category = categoryRepository.save(Category.builder()
                    .name(item.category())
                    .userId(item.userId())
                    .build());
        }

        return TransactionJpaEntity.builder()
                .amount(item.amount())
                .description(item.description())
                .occurredAt(localDate.atStartOfDay().toInstant(ZoneOffset.UTC))
                .type(TransactionType.valueOf(item.type()))
                .userId(item.userId())
                .category(categoryMapper.toJpaEntity(category))
                .build();
    }
}
