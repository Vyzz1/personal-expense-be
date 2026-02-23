package com.huynh.personal_expense_be.modules.transaction.application.port.in;

import com.huynh.personal_expense_be.modules.transaction.application.dto.CreateTransactionCommand;
import com.huynh.personal_expense_be.modules.transaction.application.dto.TransactionResponse;

import java.util.UUID;

public interface UpdateTransactionUseCase {

        TransactionResponse updateTransaction(UUID transactionId, CreateTransactionCommand command);
}
