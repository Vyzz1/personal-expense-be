package com.huynh.personal_expense_be.modules.transaction.application.port.in;

public interface NotifyBatchCompletionUseCase {
    void notifyUser(String userId, int totalRead, int totalWritten, int totalSkipped);
}
