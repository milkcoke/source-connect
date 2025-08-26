#!/bin/bash

# Path to the NDJSON file
ndjson_file=$1

if [ -z "$ndjson_file" ]; then
  echo "Target ndjson file is null or empty"
  exit 1
fi

# Kafka producer properties
bootstrap_servers="localhost:9095"
topic="ingest-topic"

JAAS_CONFIG='org.apache.kafka.common.security.scram.ScramLoginModule required username="m_falcon" password="m_falcon";'

# Use jq to parse each line of the NDJSON file and produce it to Kafka
jq -r '. | @json' "$ndjson_file" |
kafka-console-producer.sh --broker-list "$bootstrap_servers" \
--topic "$topic" \
--producer-property value.serializer=org.apache.kafka.common.serialization.LongSerializer \
--producer-property retries=5 \
--producer-property enable.idempotence=true \
--producer-property request-required-acks=-1 \
--producer-property max.in.flight.requests.per.connection=1 \
--producer-property security.protocol=SASL_PLAINTEXT \
--producer-property sasl.mechanism=SCRAM-SHA-512 \
--producer-property sasl.jaas.config="$JAAS_CONFIG"
