#!/bin/bash
# Infrastructure Orchestrator Subagent
# Automates the setup of the Docker infrastructure and waits for Cassandra initialization 
# before starting the Spring Boot application.
# Usage: ./scripts/orchestrate.sh [spring-profile]
# Example: ./scripts/orchestrate.sh high-queue

PROFILE="${1:-default}"

echo "🚀 Starting infrastructure orchestration..."

# 1. Spin up Docker Compose (detached)
echo "🐳 Starting Docker Compose services (Cassandra, Prometheus, Grafana)..."
docker-compose up -d

# 2. Wait for Cassandra initialization to complete
echo "⏳ Waiting for Cassandra keyspace to be created by 'cassandra-init'..."
MAX_RETRIES=30
RETRY_COUNT=0
INIT_SUCCESS=false

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    # Check if the cassandra-init container printed the success message
    if docker logs cassandra-init 2>&1 | grep -q 'Keyspace created!'; then
        INIT_SUCCESS=true
        break
    fi
    
    echo "   ...still waiting (attempt $((RETRY_COUNT + 1))/$MAX_RETRIES)..."
    sleep 5
    RETRY_COUNT=$((RETRY_COUNT + 1))
done

if [ "$INIT_SUCCESS" = false ]; then
    echo "❌ Timeout waiting for Cassandra initialization."
    echo "Please check the logs: docker logs cassandra-init"
    exit 1
fi

echo "✅ Cassandra initialized successfully!"

# 3. Start Spring Boot Application
echo "☕ Starting Spring Boot Application (Profile: $PROFILE)..."
if [ "$PROFILE" = "default" ]; then
    mvn spring-boot:run
else
    mvn spring-boot:run -Dspring-boot.run.profiles=$PROFILE
fi
