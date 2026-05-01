pluginManagement {
  plugins {
    kotlin("jvm") version "2.3.21"
  }
}
rootProject.name = "source-connect"
include(
  "source-connector",
  "offset-manager-api",
  "offset-manager",
  "benchmarks"
)
