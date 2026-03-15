package com.huynh.personal_expense_be.modules.transaction.infrastructure.batch;

import com.huynh.personal_expense_be.modules.transaction.application.port.out.TransactionBatchPort;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TransactionBatchAdapter implements TransactionBatchPort {

    private final JobOperator jobOperator;
    private final Job importJob;

    public TransactionBatchAdapter(@Qualifier("asyncJobOperator") JobOperator jobOperator, Job importJob) {
        this.jobOperator = jobOperator;
        this.importJob = importJob;
    }

    @Override
    public void executeBatchImport(String userId, String filePath) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("userId", userId)
                    .addString("filePath", filePath)
                    .toJobParameters();
            jobOperator.start(importJob,jobParameters);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

