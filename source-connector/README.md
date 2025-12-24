# Introduction

The Source Connector provides File object connect to the Kafka topic supporting the Exactly-Once Semantic.


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
