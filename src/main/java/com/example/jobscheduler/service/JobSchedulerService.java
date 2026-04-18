package com.example.jobscheduler.service;

import com.example.jobscheduler.model.Job;
import com.example.jobscheduler.repository.JobRepository;
import io.micrometer.core.instrument.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Periodically creates new jobs in Cassandra.
 */
@Service
public class JobSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(JobSchedulerService.class);

    private final JobRepository jobRepository;
    private final AtomicInteger pendingJobsCount;     // ← GAUGE backing field
    private final Counter jobsScheduledCounter;       // ← COUNTER (monotonic)

    @Value("${app.scheduler.ttl-min:0}")
    private int ttlMin;

    @Value("${app.scheduler.ttl-max:60}")
    private int ttlMax;

    public JobSchedulerService(JobRepository jobRepository,
                               AtomicInteger pendingJobsCount,
                               Counter jobsScheduledCounter) {
        this.jobRepository = jobRepository;
        this.pendingJobsCount = pendingJobsCount;
        this.jobsScheduledCounter = jobsScheduledCounter;
    }

    /**
     * Creates a job with a random TTL. Fixed rate configurable via application.yml.
     */
    @Scheduled(fixedRateString = "${app.scheduler.fixed-rate:5000}")
    public void scheduleJob() {
        int ttl = ThreadLocalRandom.current().nextInt(ttlMin, ttlMax + 1);
        Job job = new Job(UUID.randomUUID(), "SCHEDULED", Instant.now(), ttl);

        jobRepository.save(job);

        // ┌────────────────────────────────────────────────┐
        // │  GAUGE INCREMENT: pending_jobs_total UP by 1    │
        // └────────────────────────────────────────────────┘
        int current = pendingJobsCount.incrementAndGet();

        // ┌────────────────────────────────────────────────┐
        // │  COUNTER INCREMENT: jobs_scheduled_total +1     │
        // │  (monotonic — never decreases)                  │
        // └────────────────────────────────────────────────┘
        jobsScheduledCounter.increment();

        log.info("📋 JOB SCHEDULED: {} | TTL={}s | executeAt={} | pending_jobs_total={}",
                job.getId(), ttl, job.getExecuteAt(), current);
    }
}
