package com.huynh.personal_expense_be.modules.transaction.application.port.in;

import java.util.UUID;

public interface DeleteTransactionUseCase {

    void deleteTransactionById(String userId, UUID transactionId);
}
