package com.huynh.personal_expense_be.modules.transaction.infrastructure.batch;

import com.huynh.personal_expense_be.modules.transaction.application.dto.TransactionCsv;
import com.huynh.personal_expense_be.modules.transaction.infrastructure.persistence.TransactionJpaEntity;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;

import org.springframework.batch.infrastructure.item.database.JpaItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class TransactionBatchJobConfig {

        private final EntityManagerFactory entityManagerFactory;
        private final JobRepository jobRepository;
        private final PlatformTransactionManager transactionManager;
        private final TransactionValidationProcessor transactionValidationProcessor;
        private final TransactionChunkListener transactionChunkListener;
        private final JobSummaryTasklet jobSummaryTasklet;

        @Bean
        @StepScope
        public FlatFileItemReader<TransactionCsv> csvTransactionReader(
                        @Value("#{jobParameters['filePath']}") String filePath,
                        @Value("#{jobParameters['userId']}") String userId) {

                log.info("Creating FlatFileItemReader with file path: {}", filePath);
                return new FlatFileItemReaderBuilder<TransactionCsv>()
                                .name("transactionItemReader")
                                .resource(new FileSystemResource(filePath))
                                .linesToSkip(1) // Skip header line
                                .delimited()
                                .delimiter(",")
                                .names("amount", "description", "date", "type", "category")
                                .fieldSetMapper(fieldSet -> new TransactionCsv(
                                                fieldSet.readBigDecimal("amount"),
                                                fieldSet.readString("description"),
                                                fieldSet.readString("date"),
                                                fieldSet.readString("type"),
                                                fieldSet.readString("category"), userId))
                                .build();
        }

        @Bean
        public JpaItemWriter<TransactionJpaEntity> writer() {
                return new JpaItemWriterBuilder<TransactionJpaEntity>()
                                .entityManagerFactory(entityManagerFactory)
                                .build();
        }

        @Bean
        public Step validationStep(FlatFileItemReader<TransactionCsv> csvTransactionReader) {
                return new StepBuilder("validation-step", jobRepository)
                                .<TransactionCsv, TransactionJpaEntity>chunk(10)
                                .transactionManager(transactionManager)
                                .reader(csvTransactionReader)
                                .processor(transactionValidationProcessor)
                                .writer(writer())
                                .faultTolerant()
                                .listener(transactionChunkListener)
                                .skip(IllegalArgumentException.class) // Skip invalid records
                                .skipLimit(10)
                                .skip(DataIntegrityViolationException.class)// Allow unlimited skips
                                .build();
        }

        @Bean
        public Step summaryStep() {
                return new StepBuilder("summary-step", jobRepository)
                                .tasklet(jobSummaryTasklet, transactionManager)
                                .build();
        }

        @Bean
        public Job importJob(Step validationStep, Step summaryStep) {
                return new JobBuilder("import-job", jobRepository)
                                .start(validationStep)
                                .next(summaryStep)
                                .build();
        }
}
