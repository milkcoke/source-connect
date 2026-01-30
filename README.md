

## Introduction
Source Connect provides the ETL pipeline from file source to the kafka with Exactly Once Semantics.
![Overview](./assets/source_connect_components.png)

## PreRequisites
![Open-JDK](https://img.shields.io/badge/jdk->=v25-23ED8B00?style=for-the-badge&logo=openjdk&logoColor=%23ED8B00) \
![Docker](https://img.shields.io/badge/docker-257bd6?style=for-the-badge&logo=docker&logoColor=white)

## Quick Start in local

#### (1) Launch kafka and kafka-ui
```bash
# Execute kafka brokers and kafka-ui in your host machine
$ docker-compose up -d
```

Check the kafka-ui at http://localhost:9090

![Kafka-UI](./assets/kafka_ui_preview.png)

#### (2) Configure the application.yml

Edit the `application.yml` file located in `source-connector/src/main/resources/application.yml` as below:

```yaml
source:
  storage:
    type: local
    paths:
      - "file:///path/to/your/directory"

    configs:
      recursive: true
      filters:
```

This configuration let source connector to read files from the specified local directory. \
And produce it to the kafka topic

#### (3) Execute the Source Connector
Set the `JOB_INDEX` environment variable for specifying single worker index 
```bash
$ JOB_INDEX=0 ./gradlew :source-connector:bootRun
```

#### (4) Verify the produced messages in kafka-ui
Go to the kafka-ui at http://localhost:9090 \
Select the topic named `sink-topic` and check the messages produced from the source connector.

![Kafka-UI-Messages](./assets/kafka_ui_result.png)

#### (Optional) Execute Offset Manager

```bash
$ ./gradlew :offset-manager:bootRun
```

You should edit the `offsetManager` property in `application.yml` in source-connector module as below:
```yaml
offsetManager:
  type: http
  baseUrl: http://localhost:8080
```


## Documentation
Design notes and usage information can be found in the [wiki](https://github.com/milkcoke/source-connect/wiki)
