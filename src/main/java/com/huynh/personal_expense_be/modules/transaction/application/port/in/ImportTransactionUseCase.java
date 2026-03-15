package com.huynh.personal_expense_be.modules.transaction.application.port.in;

import com.huynh.personal_expense_be.modules.transaction.application.dto.ImportTransactionCommand;
import com.huynh.personal_expense_be.modules.transaction.application.dto.TransactionBatchResponse;

public interface ImportTransactionUseCase {

    TransactionBatchResponse importTransactions(ImportTransactionCommand command);
}
