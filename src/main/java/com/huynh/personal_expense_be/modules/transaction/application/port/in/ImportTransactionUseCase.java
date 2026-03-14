package com.huynh.personal_expense_be.modules.transaction.application.port.in;

import com.huynh.personal_expense_be.modules.transaction.application.dto.ImportTransactionCommand;

public interface ImportTransactionUseCase {

    void importTransactions(ImportTransactionCommand command);
}
