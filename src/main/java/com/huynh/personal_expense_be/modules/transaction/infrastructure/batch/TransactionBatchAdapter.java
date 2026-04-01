package com.huynh.personal_expense_be.modules.transaction.infrastructure.batch;

import com.huynh.personal_expense_be.modules.transaction.application.port.out.TransactionBatchPort;
import com.huynh.personal_expense_be.modules.transaction.domain.batch.BatchJob;
import com.huynh.personal_expense_be.shared.exception.NotFoundException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransactionBatchAdapter implements TransactionBatchPort {

    private final JobOperator jobOperator;
    private final Job importJob;
    private final JobRepository jobRepository;

    public TransactionBatchAdapter(@Qualifier("asyncJobOperator") JobOperator jobOperator, Job importJob,
            JobRepository jobRepository) {
        this.jobOperator = jobOperator;
        this.importJob = importJob;
        this.jobRepository = jobRepository;

    }

    @Override
    public BatchJob executeBatchImport(String userId, String filePath) {
        try {
            log.info("Starting transaction batch import. userId={}, filePath={}", userId, filePath);

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("userId", userId)
                    .addLong("time", System.currentTimeMillis())
                    .addString("filePath", filePath)
                    .toJobParameters();
            JobExecution jobExecution = jobOperator.start(importJob, jobParameters);

            return BatchJob.builder()
                    .batchId(String.valueOf(jobExecution.getId()))
                    .status(jobExecution.getStatus().toString())
                    .build();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BatchJob getBatchImportStatus(String batchId) {

        JobExecution jobExecution = jobRepository.getJobExecution(Long.parseLong(batchId));

        if (jobExecution == null) {
            throw new NotFoundException("Batch job not found");
        }

        return BatchJob.builder()
                .batchId(batchId)
                .status(jobExecution.getStatus().toString())
                .build();

    }
}
