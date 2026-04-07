package com.huynh.personal_expense_be.modules.transaction.application.port.out;


import com.huynh.personal_expense_be.shared.dto.SseNotificationMessage;

public interface TransactionNotificationPort {

    void sendTransactionNotification(String userId, SseNotificationMessage notificationMessage);
}
