# Concepts

S3 Source Connector 는 ETL 패턴의 애플리케이션입니다.

3가지 컴포넌트로 구성됩니다.

1. Reader
2. Processor
3. Producer

각각의 컴포넌트는 Blocking Queue 를 두고 동작합니다.

## Pipeline
Reader -> Queue -> Processor(s) -> Queue -> Producer
