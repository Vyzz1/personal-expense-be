package com.huynh.personal_expense_be.modules.budget.application.port.out;

import com.huynh.personal_expense_be.shared.dto.SseNotificationMessage;

public interface BudgetNotificationPort {
    void sendBudgetNotification(String userId, SseNotificationMessage notificationMessage);
}
