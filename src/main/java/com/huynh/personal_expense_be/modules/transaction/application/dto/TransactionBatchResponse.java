package com.huynh.personal_expense_be.modules.transaction.application.dto;

import com.huynh.personal_expense_be.modules.transaction.domain.batch.BatchJob;

public record TransactionBatchResponse(

                String jobId,
                String jobStatus

) {

        public static TransactionBatchResponse fromBatchJob(BatchJob batchJob) {
                return new TransactionBatchResponse(batchJob.getBatchId(), batchJob.getStatus());
        }
}
