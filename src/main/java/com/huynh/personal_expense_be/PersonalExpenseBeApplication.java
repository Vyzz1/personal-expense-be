package com.huynh.personal_expense_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@EnableScheduling
@SpringBootApplication
public class PersonalExpenseBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(PersonalExpenseBeApplication.class, args);
	}

}
