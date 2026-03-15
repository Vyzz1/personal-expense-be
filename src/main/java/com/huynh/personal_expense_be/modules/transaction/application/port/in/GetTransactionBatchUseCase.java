package com.huynh.personal_expense_be.modules.transaction.application.port.in;

import com.huynh.personal_expense_be.modules.transaction.application.dto.TransactionBatchResponse;

public interface GetTransactionBatchUseCase {

    TransactionBatchResponse getBatchImportStatus(String batchId);
}
