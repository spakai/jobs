#!/bin/bash
# Prometheus Metrics Verifier Skill
# Usage: ./scripts/verify-metrics.sh [METRIC_NAME]
# Default checks for 'job_execution_time' if no metric name provided.

METRIC="${1:-job_execution_time}"
ENDPOINT="http://localhost:8080/actuator/prometheus"

echo "Checking for metric '$METRIC' at $ENDPOINT..."
echo ""

# Fetch metrics and grep for the specified metric
OUTPUT=$(curl -s $ENDPOINT | grep -i "$METRIC")

if [ -z "$OUTPUT" ]; then
    echo "❌ Metric '$METRIC' NOT FOUND or application is not running."
    exit 1
else
    echo "✅ Metric '$METRIC' FOUND:"
    echo "--------------------------"
    echo "$OUTPUT"
fi
