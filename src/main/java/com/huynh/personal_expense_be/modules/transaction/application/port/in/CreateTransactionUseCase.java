package com.huynh.personal_expense_be.modules.transaction.application.port.in;

import com.huynh.personal_expense_be.modules.transaction.application.dto.CreateTransactionCommand;
import com.huynh.personal_expense_be.modules.transaction.application.dto.TransactionResponse;

public interface CreateTransactionUseCase {

    TransactionResponse createTransaction(CreateTransactionCommand command);
}
