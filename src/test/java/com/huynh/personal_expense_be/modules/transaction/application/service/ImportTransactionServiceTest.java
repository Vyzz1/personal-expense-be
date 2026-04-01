package com.huynh.personal_expense_be.modules.transaction.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.huynh.personal_expense_be.modules.transaction.application.dto.ImportTransactionCommand;
import com.huynh.personal_expense_be.modules.transaction.application.port.out.TransactionBatchPort;
import com.huynh.personal_expense_be.modules.transaction.domain.batch.BatchJob;

@ExtendWith(MockitoExtension.class)
public class ImportTransactionServiceTest {

    @Mock
    private TransactionBatchPort transactionBatchPort;

    @InjectMocks
    private ImportTransactionService importTransactionService;

    @Test
    void importTransactions_shouldExecuteBatchImport() {
        String userId = "user1";
        String filePath = "path/to/transactions.csv";

        BatchJob batchJob = BatchJob.builder()
                .batchId("batch123")
                .status("IN_PROGRESS")
                .build();

        when(transactionBatchPort.executeBatchImport(userId, filePath)).thenReturn(batchJob);

        var response = importTransactionService.importTransactions(
                new ImportTransactionCommand(userId, filePath));

        verify(transactionBatchPort).executeBatchImport(userId, filePath);

        assertThat(response).isNotNull();
        assertThat(response.jobId()).isEqualTo(batchJob.getBatchId());
        assertThat(response.jobStatus()).isEqualTo(batchJob.getStatus());

    }

    @Test
    void getBatchImportStatus_shouldReturnBatchJob() {
        String batchId = "batch123";

        BatchJob batchJob = BatchJob.builder()
                .batchId(batchId)
                .status("COMPLETED")
                .build();

        when(transactionBatchPort.getBatchImportStatus(batchId)).thenReturn(batchJob);

        var response = importTransactionService.getBatchImportStatus(batchId);

        verify(transactionBatchPort).getBatchImportStatus(batchId);

        assertThat(response).isNotNull();
        assertThat(response.jobId()).isEqualTo(batchJob.getBatchId());
        assertThat(response.jobStatus()).isEqualTo(batchJob.getStatus());
    }

}
