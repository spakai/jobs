#!/bin/bash
# Cassandra Query Skill
# Automates querying the local Cassandra Docker container.
# Usage: ./scripts/cql-query.sh "SELECT * FROM jobs LIMIT 10;"

QUERY="${1:-SELECT * FROM jobs LIMIT 10;}"

echo "Running query against 'job_scheduler' keyspace: $QUERY"
echo ""

docker exec -t cassandra cqlsh -k job_scheduler -e "$QUERY"
