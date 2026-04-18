# AI Skills (`skills.md`)

This file defines the AI "Skills" (or custom instructions/tool patterns) that can be used by an AI assistant in this repository. These skills provide the AI with the necessary context and exact commands it should use to interact with the project.

## Skill: Cassandra Querying
**Description**: Gives the AI the ability to directly query the database to verify state without requiring the user to do it manually.
**Trigger**: When the user asks to "check the database", "view job state", or "debug Cassandra".
**Instructions for AI**:
1. Do not ask the user to run CQL queries.
2. Use your terminal execution tool to run queries directly against the Cassandra Docker container.
3. Base command to use: `docker exec -t cassandra cqlsh -k job_scheduler -e "<YOUR_QUERY>"`
4. When querying for jobs, always limit the results to avoid overwhelming the console (e.g., `LIMIT 10`).

## Skill: Verify Exposing Metrics
**Description**: Allows the AI to check if Micrometer metrics are actively being exposed on the running Spring Boot Prometheus endpoint.
**Trigger**: When the user adds a new metric (Counter, Timer, Gauge) or asks to verify Prometheus.
**Instructions for AI**:
1. Use your terminal execution tool to curl the actuator endpoint: `curl -s http://localhost:8080/actuator/prometheus`
2. Search the output for the specific metric string requested.
3. If the metric is missing, automatically check `application.yml` to ensure `management.endpoints.web.exposure.include` contains `prometheus`.

## Skill: Grafana Dashboard Validation
**Description**: Ensures the AI knows where dashboards are provisioned and how they map to the metrics.
**Trigger**: When the user modifies `grafana/dashboards/*.json` or creates a new metric that needs visualizing.
**Instructions for AI**:
1. When a new metric is created in Java code, proactively offer to add a corresponding JSON panel in `grafana/dashboards/`.
2. Ensure you map the prometheus query exactly as exposed by Spring Boot Actuator (e.g., if metric is `job.execution`, look for `job_execution_seconds` in the query).
3. Inform the user that saving the file will require a Grafana restart unless dashboard auto-reloading is configured.
