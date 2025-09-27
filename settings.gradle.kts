plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "source-connect"
include("kafka-streams-connect")
include("source-connector")
include("offset-manager-api")
include("offset-manager-local")
include("offset-manager-remote")
