package com.huynh.personal_expense_be.modules.expense.infrastructure.notification;

import org.springframework.stereotype.Component;

import com.huynh.personal_expense_be.modules.expense.application.port.out.ExpenseNotificationPort;
import com.huynh.personal_expense_be.shared.dto.SseNotificationMessage;
import com.huynh.personal_expense_be.shared.notification.service.SseNotificationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SseExpenseNotificationAdapter implements ExpenseNotificationPort {

    private final SseNotificationService sseNotificationService;

    @Override
    public void sendExpenseNotification(String userId, SseNotificationMessage notificationMessage) {
        sseNotificationService.sendNotification(userId, notificationMessage);
    }
}