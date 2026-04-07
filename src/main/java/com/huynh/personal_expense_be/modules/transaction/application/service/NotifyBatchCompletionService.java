package com.huynh.personal_expense_be.modules.transaction.application.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.huynh.personal_expense_be.modules.transaction.application.port.in.NotifyBatchCompletionUseCase;
import com.huynh.personal_expense_be.modules.transaction.application.port.out.TransactionNotificationPort;
import com.huynh.personal_expense_be.shared.dto.SseNotificationEventType;
import com.huynh.personal_expense_be.shared.dto.SseNotificationMessage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotifyBatchCompletionService implements NotifyBatchCompletionUseCase {

    private final TransactionNotificationPort notificationPort;

    @Override
    public void notifyUser(String userId, int totalRead, int totalWritten, int totalSkipped) {
        String message = "Import csv batch processing completed.";

        String description = String.format("Total Read: %d, Total Written: %d, Total Skipped: %d", totalRead, totalWritten, totalSkipped);
                                       
        SseNotificationMessage notificationMsg  = SseNotificationMessage.builder()
            .id(UUID.randomUUID().toString())
            .eventType(SseNotificationEventType.TRANSACTION_BATCH_PROCESSED)
            .message(message)
            .description(description)
            .timestamp(Instant.now())
            .build();
        
        notificationPort.sendTransactionNotification(userId, notificationMsg);
    }
}
