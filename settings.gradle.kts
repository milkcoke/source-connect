plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "s3-source-connect"
include("kafka-streams-connect")
include("s3-source-connector")
include("offset-manager-api")
include("offset-manager-local")
include("offset-manager-remote")
