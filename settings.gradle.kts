pluginManagement {
  plugins {
    kotlin("jvm") version "2.3.0"
  }
}
rootProject.name = "source-connect"
include(
  "kafka-streams-connect",
  "source-connector",
  "offset-manager-api",
  "offset-manager",
  "benchmarks"
)
