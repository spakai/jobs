package com.example.jobscheduler.service;

import com.example.jobscheduler.model.Job;
import com.example.jobscheduler.repository.JobRepository;
import io.micrometer.core.instrument.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Polls Cassandra every 2 seconds for jobs whose TTL has expired.
 *
 * When an expired job is found:
 *   1. Print "Job executed"
 *   2. Update status to EXECUTED in Cassandra
 *   3. DECREMENT the pending_jobs gauge by 1
 *   4. INCREMENT the jobs_executed counter by 1
 *
 * BUG FIX: Uses a ConcurrentHashMap to prevent the same job from
 * being executed twice (race condition between poll intervals).
 */
@Service
public class JobExecutorService {

    private static final Logger log = LoggerFactory.getLogger(JobExecutorService.class);

    private final JobRepository jobRepository;
    private final AtomicInteger pendingJobsCount;     // ← GAUGE backing field
    private final Counter jobsExecutedCounter;        // ← COUNTER (monotonic)

    /**
     * Track job IDs currently being processed to prevent double-execution.
     * Without this, two consecutive polls could pick up the same job
     * before the status update is written, causing gauge to go negative.
     */
    private final Set<java.util.UUID> inProgress = ConcurrentHashMap.newKeySet();

    public JobExecutorService(JobRepository jobRepository,
                              AtomicInteger pendingJobsCount,
                              Counter jobsExecutedCounter) {
        this.jobRepository = jobRepository;
        this.pendingJobsCount = pendingJobsCount;
        this.jobsExecutedCounter = jobsExecutedCounter;
    }

    /**
     * Polls for expired jobs. Rate configurable via application.yml.
     */
    @Scheduled(fixedRateString = "${app.executor.fixed-rate:2000}")
    public void executeExpiredJobs() {
        List<Job> expiredJobs = jobRepository.findExpiredJobs("SCHEDULED", Instant.now());

        for (Job job : expiredJobs) {
            // ── Guard: skip if already being processed ─────────
            if (!inProgress.add(job.getId())) {
                log.debug("⏭️ Skipping job {} — already in progress", job.getId());
                continue;
            }

            try {
                // ── 1. Execute the job ─────────────────────────────
                System.out.println("✅ Job executed: " + job.getId());

                // ── 2. Mark as EXECUTED in Cassandra ───────────────
                job.setStatus("EXECUTED");
                jobRepository.save(job);

                // ┌────────────────────────────────────────────────┐
                // │  GAUGE DECREMENT: pending_jobs_total DOWN by 1  │
                // │  Guard: never go below 0                        │
                // └────────────────────────────────────────────────┘
                int current = pendingJobsCount.updateAndGet(v -> Math.max(0, v - 1));

                // ┌────────────────────────────────────────────────┐
                // │  COUNTER INCREMENT: jobs_executed_total +1       │
                // │  (monotonic — never decreases)                  │
                // └────────────────────────────────────────────────┘
                jobsExecutedCounter.increment();

                log.info("🚀 JOB EXECUTED:  {} | was TTL={}s | pending_jobs_total={}",
                        job.getId(), job.getTtlSeconds(), current);
            } finally {
                inProgress.remove(job.getId());
            }
        }
    }
}
