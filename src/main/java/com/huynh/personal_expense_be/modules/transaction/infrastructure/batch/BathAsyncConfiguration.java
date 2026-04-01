package com.huynh.personal_expense_be.modules.transaction.infrastructure.batch;

import org.springframework.batch.core.launch.support.JobOperatorFactoryBean;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@Configuration
public class BathAsyncConfiguration {

    @Bean(name = "asyncJobOperator")
    public JobOperatorFactoryBean jobOperatorFactoryBean(JobRepository jobRepository) {
        JobOperatorFactoryBean factoryBean = new JobOperatorFactoryBean();
        factoryBean.setTaskExecutor(new SimpleAsyncTaskExecutor());
        factoryBean.setJobRepository(jobRepository);
        return factoryBean;
    }
}
