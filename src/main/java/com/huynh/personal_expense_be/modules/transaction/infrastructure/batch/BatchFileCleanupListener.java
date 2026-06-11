package com.huynh.personal_expense_be.modules.transaction.infrastructure.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Component
public class BatchFileCleanupListener implements JobExecutionListener {

    @Override
    public void afterJob(JobExecution jobExecution) {
        String filePath = jobExecution.getJobParameters().getString("filePath");
        if (filePath != null) {
            try {
                Files.deleteIfExists(Path.of(filePath));
                log.info("Cleaned up batch import file after job completion");
            } catch (IOException e) {
                log.warn("Failed to delete batch import file", e);
            }
        }
    }
}
