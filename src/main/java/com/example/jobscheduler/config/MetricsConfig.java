package com.example.jobscheduler.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * ┌───────────────────────────────────────────────────────────────┐
 * │                  PROMETHEUS METRICS CONFIG                     │
 * ├───────────────────────────────────────────────────────────────┤
 * │                                                               │
 * │  GAUGE   – pending.jobs.total   → goes UP and DOWN            │
 * │  COUNTER – jobs.scheduled.total → only goes UP (monotonic)    │
 * │  COUNTER – jobs.executed.total  → only goes UP (monotonic)    │
 * │                                                               │
 * │  This lets the Grafana dashboard show:                        │
 * │    • Real-time pending jobs (Gauge)                           │
 * │    • Cumulative totals (Counters)                             │
 * │    • Rates: jobs/sec scheduled vs executed (rate on Counters) │
 * │                                                               │
 * └───────────────────────────────────────────────────────────────┘
 */
@Configuration
public class MetricsConfig {

    /**
     * Thread-safe integer backing the GAUGE.
     *  - incrementAndGet() when a job is SCHEDULED
     *  - decrementAndGet() when a job is EXECUTED
     */
    @Bean
    public AtomicInteger pendingJobsCount() {
        return new AtomicInteger(0);
    }

    /**
     * GAUGE: Current pending jobs — goes up AND down.
     */
    @Bean
    public Gauge pendingJobsGauge(MeterRegistry registry, AtomicInteger pendingJobsCount) {
        return Gauge.builder("pending.jobs.total", pendingJobsCount, AtomicInteger::get)
                .description("Current number of pending (scheduled but not yet executed) jobs")
                .tag("type", "job_scheduler")
                .register(registry);
    }

    /**
     * COUNTER: Total jobs scheduled — only goes UP.
     * Prometheus name: jobs_scheduled_total
     */
    @Bean
    public Counter jobsScheduledCounter(MeterRegistry registry) {
        return Counter.builder("jobs.scheduled.total")
                .description("Total number of jobs scheduled (monotonic counter)")
                .tag("type", "job_scheduler")
                .register(registry);
    }

    /**
     * COUNTER: Total jobs executed — only goes UP.
     * Prometheus name: jobs_executed_total
     */
    @Bean
    public Counter jobsExecutedCounter(MeterRegistry registry) {
        return Counter.builder("jobs.executed.total")
                .description("Total number of jobs executed (monotonic counter)")
                .tag("type", "job_scheduler")
                .register(registry);
    }

    /**
     * COUNTER: Total jobs failed — only goes UP.
     * Prometheus name: jobs_failed_total
     */
    @Bean
    public Counter jobsFailedCounter(MeterRegistry registry) {
        return Counter.builder("jobs.failed.total")
                .description("Total number of jobs failed to schedule or execute")
                .tag("type", "job_scheduler")
                .register(registry);
    }
}
