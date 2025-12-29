# Introduction

The Source Connector provides File object connect to the Kafka topic supporting the Exactly-Once Semantic.

## Prerequisites
![Open-JDK](https://img.shields.io/badge/jdk->=v21-23ED8B00?style=for-the-badge&logo=openjdk&logoColor=%23ED8B00)


## Configuration
```yaml
app:
  jobCount: 1 # should be greater than or equal to the 1
  offsetManagerBaseUrl: localhost://8080 # OffsetManager base url

  storageType: local # local, s3, azure
  filePaths:
    - C://Users/milkcoke/Downloads/logs
    - C://Users/milkcoke/Downloads/logs2
```
