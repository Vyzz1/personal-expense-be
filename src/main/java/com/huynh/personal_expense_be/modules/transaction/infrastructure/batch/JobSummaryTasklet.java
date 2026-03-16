package com.huynh.personal_expense_be.modules.transaction.infrastructure.batch;

import java.time.Duration;
import java.time.LocalDateTime;

import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JobSummaryTasklet implements Tasklet {

    @Override
    public @Nullable RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        StepExecution stepExecution = chunkContext.getStepContext().getStepExecution();
        var jobExecution = stepExecution.getJobExecution();

        int totalRead = jobExecution.getStepExecutions().stream()
                .mapToInt(step -> (int) step.getReadCount())
                .sum();

        int totalWritten = jobExecution.getStepExecutions().stream()
                .mapToInt(step -> (int) step.getWriteCount())
                .sum();

        int totalSkipped = jobExecution.getStepExecutions().stream()
                .mapToInt(step -> (int) step.getSkipCount())
                .sum();

        int totalFiltered = jobExecution.getStepExecutions().stream()
                .mapToInt(step -> (int) step.getFilterCount())
                .sum();

        // Calculate duration
        LocalDateTime startTime = jobExecution.getStartTime();
        LocalDateTime endTime = jobExecution.getEndTime();

        long durationMs = 0;
        if (startTime != null && endTime != null) {
            Duration duration = Duration.between(startTime, endTime);
            durationMs = duration.toMillis();
        }

        log.info("========================================");
        log.info("JOB SUMMARY GENERATED");
        log.info("Total Processed: {}", totalRead);
        log.info("Successful: {}", totalWritten);
        log.info("Skipped: {}", totalSkipped);
        log.info("Filtered: {}", totalFiltered);
        log.info("Duration: {} ms", durationMs);
        log.info("========================================");

        return RepeatStatus.FINISHED;
    }

}
