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

import com.huynh.personal_expense_be.modules.transaction.application.port.in.NotifyBatchCompletionUseCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class JobSummaryTasklet implements Tasklet {

    private final NotifyBatchCompletionUseCase notifyBatchCompletionUseCase;

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

        String userId = jobExecution.getJobParameters().getString("userId");
        if (userId != null && !userId.isBlank()) {
            notifyBatchCompletionUseCase.notifyUser(userId, totalRead, totalWritten, totalSkipped);
            log.info("SSE Notification sent to user: {}", userId);
        } else {
            log.warn("userId not found in job parameters. Cannot push SSE notification.");
        }

        return RepeatStatus.FINISHED;
    }

}
