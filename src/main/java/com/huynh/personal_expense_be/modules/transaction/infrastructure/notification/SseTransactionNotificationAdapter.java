package com.huynh.personal_expense_be.modules.transaction.infrastructure.notification;

import org.springframework.stereotype.Component;

import com.huynh.personal_expense_be.modules.transaction.application.port.out.TransactionNotificationPort;
import com.huynh.personal_expense_be.shared.dto.SseNotificationMessage;
import com.huynh.personal_expense_be.shared.notification.service.SseNotificationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SseTransactionNotificationAdapter implements TransactionNotificationPort {

    private final SseNotificationService sseNotificationService;

    @Override
    public void sendTransactionNotification(String userId, SseNotificationMessage notificationMessage) {
        sseNotificationService.sendNotification(userId, notificationMessage);
    }
}
