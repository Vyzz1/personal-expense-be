package com.huynh.personal_expense_be.modules.transaction.application.service;

import com.huynh.personal_expense_be.modules.transaction.application.dto.ImportTransactionCommand;
import com.huynh.personal_expense_be.modules.transaction.application.port.in.ImportTransactionUseCase;
import com.huynh.personal_expense_be.modules.transaction.application.port.out.TransactionBatchPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImportTransactionService implements ImportTransactionUseCase {

    private final TransactionBatchPort transactionBatchPort;


    @Override
    public void importTransactions(ImportTransactionCommand command) {
        transactionBatchPort.executeBatchImport(command.userId(), command.filePath());
    }
}
