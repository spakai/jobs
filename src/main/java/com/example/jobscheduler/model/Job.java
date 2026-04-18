package com.example.jobscheduler.model;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.mapping.Column;

import java.time.Instant;
import java.util.UUID;

/**
 * Cassandra table: jobs
 *
 * Each row represents a scheduled job.
 *  - id:          unique job identifier
 *  - status:      SCHEDULED | EXECUTED
 *  - createdAt:   when the job was inserted
 *  - ttlSeconds:  random value 0–60; the job "expires" after this many seconds
 *  - executeAt:   createdAt + ttlSeconds  (pre-computed for easy querying)
 */
@Table("jobs")
public class Job {

    @PrimaryKey
    private UUID id;

    @Column("status")
    private String status;

    @Column("created_at")
    private Instant createdAt;

    @Column("ttl_seconds")
    private int ttlSeconds;

    @Column("execute_at")
    private Instant executeAt;

    public Job() {}

    public Job(UUID id, String status, Instant createdAt, int ttlSeconds) {
        this.id = id;
        this.status = status;
        this.createdAt = createdAt;
        this.ttlSeconds = ttlSeconds;
        this.executeAt = createdAt.plusSeconds(ttlSeconds);
    }

    // ── Getters & Setters ──────────────────────────────────────

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public int getTtlSeconds() { return ttlSeconds; }
    public void setTtlSeconds(int ttlSeconds) { this.ttlSeconds = ttlSeconds; }

    public Instant getExecuteAt() { return executeAt; }
    public void setExecuteAt(Instant executeAt) { this.executeAt = executeAt; }

    @Override
    public String toString() {
        return String.format("Job{id=%s, status='%s', ttl=%ds, executeAt=%s}",
                id, status, ttlSeconds, executeAt);
    }
}
