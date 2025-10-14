
# JMH benchmarks

A collection of JMH benchmarks which are useful for measuring source-connect performance.

## How to start

#### (1) Generate dummy data
You should generate 100MiB * 10 ndjson files before executing FileSourceTaskBenchmark.java \
Execute this command 
```bash
$ cd src/jmh/resources/large-testdata
$ node generate_ndjosn.js
```

#### (2) Execute jmh task
```bash
$ ./gradlew :benchmarks:jmh
```
