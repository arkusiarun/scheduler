package com.airtel.scheduler.execution;

import com.airtel.scheduler.execution.constants.CommonConstants;
import org.jobrunr.spring.autoconfigure.JobRunrAutoConfiguration;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * @author Arun Singh
 */

@EnableMongoRepositories(basePackages = {CommonConstants.BASE_PACKAGE})
@SpringBootApplication(exclude = {MongoAutoConfiguration.class, JobRunrAutoConfiguration.class})
@ComponentScan(basePackages = {CommonConstants.BASE_PACKAGE})
public class SchedulerApplication  implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(SchedulerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Thread.currentThread().join();
    }
}