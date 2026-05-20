package com.huynh.personal_expense_be.shared.infrastructure.worker;

import com.huynh.personal_expense_be.shared.infrastructure.persistence.OutboxMessageJpaEntity;
import com.huynh.personal_expense_be.shared.infrastructure.persistence.OutboxPersistenceJpaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

@Component
public class OutboxWorker {

    private static final int BATCH_SIZE = 50;
    private static final int MAX_ATTEMPTS = 10;
    private static final long BASE_RETRY_SECONDS = 15L;
    private static final long MAX_RETRY_SECONDS = 3600L;

    private final OutboxPersistenceJpaRepository outboxPersistenceJpaRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final TransactionTemplate requiresNewTransactionTemplate;

    private final Logger log = LoggerFactory.getLogger(OutboxWorker.class);

    public OutboxWorker(OutboxPersistenceJpaRepository outboxPersistenceJpaRepository,
                        ObjectMapper objectMapper,
                        ApplicationEventPublisher eventPublisher,
                        PlatformTransactionManager transactionManager) {
        this.outboxPersistenceJpaRepository = outboxPersistenceJpaRepository;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
        this.requiresNewTransactionTemplate = new TransactionTemplate(transactionManager);
        this.requiresNewTransactionTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
    }

    @Scheduled(fixedDelay = 15000)
    public void process() {
        int processedCount = 0;
        for (int i = 0; i < BATCH_SIZE; i++) {
            Boolean handled = requiresNewTransactionTemplate.execute(status -> processOneMessage());
            if (!Boolean.TRUE.equals(handled)) {
                break;
            }
            processedCount++;
        }
        log.info("Outbox worker processed {} message(s) this cycle", processedCount);
    }

    private boolean processOneMessage() {
        List<OutboxMessageJpaEntity> messages = outboxPersistenceJpaRepository.pollProcessableMessages(1);
        if (messages.isEmpty()) {
            return false;
        }
        processMessage(messages.getFirst());
        return true;
    }

    private void processMessage(OutboxMessageJpaEntity message) {
        try {
            Class<?> eventClass = Class.forName(message.getEventType());
            Object event = objectMapper.readValue(message.getPayload(), eventClass);

            log.info("Processing message with ID: {}, Event Type: {}", message.getId(), message.getEventType());
            log.info("Received event: {}", event.getClass().getSimpleName());

            eventPublisher.publishEvent(event);

            message.setProcessedAt(Instant.now());
            message.setError(null);
            message.setNextRetryAt(null);

        } catch (Exception e) {
            log.error("Error processing message with ID: {}, Event Type: {}, Error: {}", message.getId(),
                    message.getEventType(), e.getMessage());
            int attemptCount = message.getAttemptCount() == null ? 0 : message.getAttemptCount();
            attemptCount++;
            message.setAttemptCount(attemptCount);
            message.setError(e.getMessage());
            if (attemptCount >= MAX_ATTEMPTS) {
                message.setProcessedAt(Instant.now());
                message.setNextRetryAt(null);
            } else {
                message.setNextRetryAt(Instant.now().plusSeconds(calculateRetryDelaySeconds(attemptCount)));
            }
        } finally {
            outboxPersistenceJpaRepository.save(message);
        }
    }

    private long calculateRetryDelaySeconds(int attemptCount) {
        int exponent = Math.min(attemptCount - 1, 10);
        long delay = BASE_RETRY_SECONDS * (1L << exponent);
        return Math.min(delay, MAX_RETRY_SECONDS);
    }
}
