#!/bin/bash

# Define the required variables
BOOTSTRAP_SERVERS="localhost:9095"
TOPIC="retry-topic"
OUT_DIR="data/output"
OUT_FILENAME="$TOPIC.ndjson"

# Create a JAAS configuration string
JAAS_CONFIG='org.apache.kafka.common.security.scram.ScramLoginModule required username="m_falcon" password="m_falcon";'

CURRENT_DATETIME=$(date +"%Y-%m-%dT%H:%M:%S")
GROUP_ID="s3-retryer-$CURRENT_DATETIME"

# Run the Kafka console consumer and redirect the output to a file
kafka-console-consumer.sh --bootstrap-server $BOOTSTRAP_SERVERS \
--topic $TOPIC \
--from-beginning \
--property print.key=false \
--property key.separator="" \
--consumer-property group.id="$GROUP_ID" \
--consumer-property security.protocol=SASL_PLAINTEXT \
--consumer-property sasl.mechanism=SCRAM-SHA-512 \
--consumer-property sasl.jaas.config="$JAAS_CONFIG" \
> "$OUT_DIR/$OUT_FILENAME"
