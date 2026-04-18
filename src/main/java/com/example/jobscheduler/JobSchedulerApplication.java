package com.example.jobscheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point.
 *
 * @EnableScheduling activates Spring's task scheduler so our
 * @Scheduled methods in JobSchedulerService and JobExecutorService run.
 */
@SpringBootApplication
@EnableScheduling
public class JobSchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobSchedulerApplication.class, args);
    }
}
