package com.huynh.personal_expense_be.shared.infrastructure.persistence;

import com.huynh.personal_expense_be.shared.application.port.out.OutboxRepositoryPort;
import com.huynh.personal_expense_be.shared.domain.DomainEvent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class OutboxPersistenceAdapter  implements OutboxRepositoryPort {

    @PersistenceContext
    private EntityManager entityManager;

    private final ObjectMapper objectMapper;

    @Override
    public void saveOutboxMessage(DomainEvent event, String module) {
       try {
        
         OutboxMessageJpaEntity outboxMessage = new OutboxMessageJpaEntity();
        outboxMessage.setModule(module);
        outboxMessage.setPayload(objectMapper.writeValueAsString(event));
        outboxMessage.setEventType(event.getClass().getName());
        entityManager.persist(outboxMessage);

       } catch (Exception e) {

        throw e;
       }
    }

  
}
