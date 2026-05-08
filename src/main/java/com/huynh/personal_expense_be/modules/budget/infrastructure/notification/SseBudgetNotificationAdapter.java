package com.huynh.personal_expense_be.modules.budget.infrastructure.notification;

import com.huynh.personal_expense_be.modules.budget.application.port.out.BudgetNotificationPort;
import com.huynh.personal_expense_be.shared.dto.SseNotificationMessage;
import com.huynh.personal_expense_be.shared.notification.service.SseNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SseBudgetNotificationAdapter implements BudgetNotificationPort {

    private final SseNotificationService sseNotificationService;

    @Override
    public void sendBudgetNotification(String userId, SseNotificationMessage notificationMessage) {
        sseNotificationService.sendNotification(userId, notificationMessage);
    }
}
