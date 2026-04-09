package com.huynh.personal_expense_be.shared.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "outbox_messages")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class OutboxMessageJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, name = "module", length = 100)
    private String module;

    @Column(nullable = false, name = "payload", columnDefinition = "text")
    private String payload;

    @CreatedDate
    @Column(nullable = false, name = "created_at")
    private Instant createdAt;

    @Column(name = "processed_at", nullable = true)
    private Instant processedAt;

    @Column(name = "error", length = 1000 , nullable = true)
    private String error;

    @Column(name = "event_type", length = 255, nullable = false)
    private String eventType;
}
