package com.huynh.personal_expense_be.modules.transaction.application.port.in;

import com.huynh.personal_expense_be.modules.transaction.application.dto.TransactionResponse;

import java.util.UUID;

public interface GetTransactionDetailUseCase {

    TransactionResponse getTransactionDetailById(UUID transactionId, String userId);
}
