# Introduction
Exactly once produce from AWS S3 to Kafka topic which s3 object consists of json record.

# Architecture
![Architecture Diagram](/assets/architecture_diagram.png)

### (1) PutObject
A Client upload source files to s3, Consider file size not too large

### (2) Produce
From (1) You can get s3 object path. \
Produce s3 object path on s3 topic

> ⚠️ ***The number of records should be same which of s3 object you put at (1) step***

#### Example
```json
{
  "s3": {
    "bucket": "",
    "path": ""
  }
}
```

### (3) Consume
This is a first step of Kafka transaction
Kafka Streams Application consume a record which consists of bucket and object path.

### (4) GetObject
Get S3 object which is retrieve from (3)

### (5) Produce
Produce all records of the object to the sink topic

# Prerequisites
![JDK-17](https://img.shields.io/badge/jdk->=v17-FF7900.svg?style=for-the-badge&logo=openjdk&logoColor=FF7900&color=FF7900)
![Apache Kafka](https://img.shields.io/badge/Kafka->=v3.5.0-F28D1A?style=for-the-badge&logo=apache-kafka&logoColor=CC6699)
![AWS-CLI](https://img.shields.io/badge/aws_cli-%3E%3Dv2.0-gray.svg?style=for-the-badge&logo=amazon-aws&labelColor=gray&color=FF7900)

# Steps

## (0) Split file
```bash
# Split the large source file into smaller ones by line number
$ scripts/splitfile.sh [target-file-path] [line-number] 
```

## (1) S3 upload and extract s3 path list file
```bash
# Upload a single file
$ aws s3 cp [local-file-path] [s3-path] | tr '\r' '\n' | grep upload | awk '{print $4}' > "uploaded_list.txt"
# Upload directory
$ aws s3 cp [local-directory-path] [s3-path] | tr '\r' '\n' | grep upload | awk '{print $4}' >> "uploaded_list.txt"
```

> ⚠️ `tr` removes the line feed character automatically

## (2) Create ndjson file for producing records 
```bash
# output file name is `records.ndjson`
$ scripts/generate_json_record.sh [list-file-path]
```

## (3) Produce s3 path records
```bash
$ scripts/produce_records.sh [target-ndjson-file]
```

## (4) Start application
```bash
$ ./gradlew bootRun
```

# FAQ
#### Q. Why should we upload s3 and produce s3 path record to the source topic?
A. Suppose Kafka producer fail in the middle of production. \
The client would not know where to start record of specific file. \
You should handle file offset to avoid duplication and keep ordering.

I choose the kafka streams processing guarantee option `exactly_once_v2`. \
This option provides a transactional mechanism for keeping ordering, no duplication, and offset handling. \
For make steps (3) ~ (6) a transaction, we should produce records to the kafka topic.
