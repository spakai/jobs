package com.example.jobscheduler.repository;

import com.example.jobscheduler.model.Job;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data Cassandra repository for the jobs table.
 *
 * NOTE: Cassandra doesn't support arbitrary WHERE clauses efficiently.
 * For this demo we use ALLOW FILTERING. In production you'd design
 * a proper partition key strategy (e.g., partition by status + time bucket).
 */
@Repository
public interface JobRepository extends CassandraRepository<Job, UUID> {

    /**
     * Find all jobs that are still SCHEDULED and whose execute_at
     * time has passed (i.e., TTL expired → ready to execute).
     */
    @Query("SELECT * FROM jobs WHERE status = ?0 AND execute_at <= ?1 ALLOW FILTERING")
    List<Job> findExpiredJobs(String status, Instant now);
}
