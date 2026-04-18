# Job Scheduler Prometheus

A Java Spring Boot application that schedules and executes jobs using Cassandra for data storage, and provides comprehensive observability using Prometheus and Grafana.

## Features

- **Job Scheduling**: Background job scheduling utilizing Spring Boot's built-in scheduling capabilities.
- **Cassandra Integration**: Stores job state and related data in Apache Cassandra.
- **Metrics & Observability**: Exposes Prometheus metrics (Gauges and Counters) through Spring Boot Actuator and Micrometer.
- **Pre-configured Dashboards**: Includes Docker setup for automated provisioning of Grafana dashboards and Prometheus data sources.
- **Containerized Stack**: Easy orchestration of infrastructure services (Cassandra, Prometheus, Grafana) via Docker Compose.

## Tech Stack

- **Java 21**
- **Spring Boot 3.2.5**
- **Spring Data Cassandra**
- **Micrometer Registry Prometheus**
- **Docker & Docker Compose**

## Getting Started

### Prerequisites

- Java 21
- Maven
- Docker and Docker Compose

### Running the Application

1. **Start the Infrastructure**
   Spin up Cassandra, Prometheus, and Grafana using Docker Compose. This will also automatically create the necessary Cassandra keyspaces.
   ```bash
   docker-compose up -d
   ```

2. **Wait for Cassandra Initialization**
   Ensure the `cassandra-init` container runs and successfully sets up the keyspace (`job_scheduler`) before proceeding to keep the application from failing on initial start.

3. **Start the Spring Boot Application**
   ```bash
   mvn spring-boot:run
   ```

   You can also run the application with different profiles to tweak the scheduling and execution behavior:

   - **`high-queue` profile**: Speeds up job creation and delays execution (simulating a backlog of jobs).
     ```bash
     mvn spring-boot:run -Dspring-boot.run.profiles=high-queue
     ```
   - **`fast-exec` profile**: Speeds up job execution and limits job TTL (simulating fast processing).
     ```bash
     mvn spring-boot:run -Dspring-boot.run.profiles=fast-exec
     ```
   
   Alternatively, you can override specific properties directly via custom command line arguments:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.arguments="--app.scheduler.fixed-rate=1000 --app.executor.fixed-rate=500"
   ```

### Accessing the Services

- **Application Endpoints**:
  - Main app operates on standard configured Spring Boot port (usually `8080`).
  - Prometheus metrics: `http://localhost:8080/actuator/prometheus`
- **Prometheus UI**: `http://localhost:9090`
- **Grafana Dashboard**: `http://localhost:3000`
  - Default credentials: `admin` / `admin`
  - Dashboards are provisioned automatically.

## Project Structure

- `src/`: Contains the Spring Boot application source code.
- `docker-compose.yml`: Defines the infrastructure services (Cassandra, Prometheus, Grafana).
- `prometheus.yml`: Configuration file for Prometheus scrape targets.
- `grafana/`: Contains Grafana provisioning settings for auto-configuring datasources and dashboards.
