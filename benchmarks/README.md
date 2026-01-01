
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

#### (2) Configure the jmh task
Configure build.gradle `jmh` task configuration \
refer to the [docs](https://github.com/melix/jmh-gradle-plugin)
```kts
jmh {
  fork.set(1)
  warmupIterations.set(1)
  iterations.set(1)
  includes.set(listOf("FileSourceTaskBenchmark.singleTaskBenchmark"))
}
```
#### (3) Execute jmh
```bash
$ ./gradlew :benchmarks:clean :benchmarks:jmh
```
