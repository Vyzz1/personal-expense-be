package com.huynh.personal_expense_be.modules.transaction.application.port.out;

public interface TransactionBatchPort {

    void executeBatchImport(String userId, String filePath);
}
