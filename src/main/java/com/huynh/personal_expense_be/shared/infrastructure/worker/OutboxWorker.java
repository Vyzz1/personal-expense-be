package com.huynh.personal_expense_be.shared.infrastructure.worker;

import com.huynh.personal_expense_be.shared.infrastructure.persistence.OutboxMessageJpaEntity;
import com.huynh.personal_expense_be.shared.infrastructure.persistence.OutboxPersistenceJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxWorker {

    private final OutboxPersistenceJpaRepository outboxPersistenceJpaRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(fixedDelay = 15000)
    @Transactional
    public void process() {
        log.info("Starting outbox worker to process messages...");
        List<OutboxMessageJpaEntity> messages = outboxPersistenceJpaRepository.findByProcessedAtIsNull();
        log.info("Fetched {} unprocessed messages", messages.size());

        for (OutboxMessageJpaEntity message : messages) {
            processMessage(message);
        }
    }

    private void processMessage(OutboxMessageJpaEntity message) {
        try {
            Class<?> eventClass = Class.forName(message.getEventType());
            Object event = objectMapper.readValue(message.getPayload(), eventClass);

            log.info("Processing message with ID: {}, Event Type: {}", message.getId(), message.getEventType());
            log.info("Received event: {}", event.getClass().getSimpleName());

            eventPublisher.publishEvent(event);

            message.setProcessedAt(Instant.now());

        } catch (Exception e) {
            log.error("Error processing message with ID: {}, Event Type: {}, Error: {}", message.getId(),
                    message.getEventType(), e.getMessage());
            message.setError(e.getMessage());
        } finally {
            outboxPersistenceJpaRepository.save(message);
        }
    }
}
