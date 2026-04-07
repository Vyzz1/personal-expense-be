package com.huynh.personal_expense_be.modules.expense.application.port.out;

import com.huynh.personal_expense_be.shared.dto.SseNotificationMessage;

public interface ExpenseNotificationPort {
    void sendExpenseNotification(String userId, SseNotificationMessage notificationMessage);
}