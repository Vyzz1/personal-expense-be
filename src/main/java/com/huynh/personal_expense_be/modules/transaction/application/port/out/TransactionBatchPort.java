package com.huynh.personal_expense_be.modules.transaction.application.port.out;

import com.huynh.personal_expense_be.modules.transaction.domain.batch.BatchJob;

public interface TransactionBatchPort {

    BatchJob executeBatchImport(String userId, String filePath);

    BatchJob getBatchImportStatus(String batchId);
}
