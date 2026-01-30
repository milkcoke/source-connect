pluginManagement {
  plugins {
    kotlin("jvm") version "2.3.0"
  }
}
rootProject.name = "source-connect"
include(
  "source-connector",
  "offset-manager-api",
  "offset-manager",
  "benchmarks"
)
