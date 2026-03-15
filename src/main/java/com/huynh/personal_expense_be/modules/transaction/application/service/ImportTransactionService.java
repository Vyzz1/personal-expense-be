package com.huynh.personal_expense_be.modules.transaction.application.service;

import com.huynh.personal_expense_be.modules.transaction.application.dto.ImportTransactionCommand;
import com.huynh.personal_expense_be.modules.transaction.application.dto.TransactionBatchResponse;
import com.huynh.personal_expense_be.modules.transaction.application.port.in.GetTransactionBatchUseCase;
import com.huynh.personal_expense_be.modules.transaction.application.port.in.ImportTransactionUseCase;
import com.huynh.personal_expense_be.modules.transaction.application.port.out.TransactionBatchPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImportTransactionService implements ImportTransactionUseCase, GetTransactionBatchUseCase {

    private final TransactionBatchPort transactionBatchPort;

    @Override
    public TransactionBatchResponse importTransactions(ImportTransactionCommand command) {
        return TransactionBatchResponse.fromBatchJob(
                transactionBatchPort.executeBatchImport(command.userId(), command.filePath()));
    }

    @Override
    public TransactionBatchResponse getBatchImportStatus(String batchId) {
        return TransactionBatchResponse.fromBatchJob(
                transactionBatchPort.getBatchImportStatus(batchId));
    }
}
