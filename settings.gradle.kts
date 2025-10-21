plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "source-connect"
include(
  "kafka-streams-connect",
  "source-connector",
  "offset-manager-api",
  "offset-manager",
  "benchmarks"
)
