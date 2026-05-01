plugins {
  kotlin("jvm")
  kotlin("plugin.spring") version "2.3.21"
  id("org.springframework.boot") version "4.0.2"
  id("io.spring.dependency-management") version "1.1.7"
  id("com.google.cloud.tools.jib") version "3.5.2"
}

group = "sourceconnect"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
  implementation(project(":offset-manager-api"))
  implementation("org.springframework.boot:spring-boot-starter")
  implementation("org.springframework.boot:spring-boot-starter-kafka")
  implementation("org.apache.kafka:kafka-clients:4.2.0")
  implementation("org.apache.commons:commons-lang3:3.20.0")
  implementation("tools.jackson.core:jackson-databind:3.1.2")
  implementation("tools.jackson.module:jackson-module-kotlin:3.0.4")
  implementation("jakarta.ws.rs:jakarta.ws.rs-api:4.0.0")
  implementation("software.amazon.awssdk:s3:2.43.1")
  implementation("com.github.luben:zstd-jni:1.5.7-7")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation(platform("org.junit:junit-bom:6.0.3"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation(platform("org.testcontainers:testcontainers-bom:2.0.5"))
  testImplementation("org.testcontainers:testcontainers")
  testImplementation("org.testcontainers:junit-jupiter")
  testImplementation("org.testcontainers:localstack")
  testImplementation("org.testcontainers:kafka")

  testImplementation("org.assertj:assertj-core:3.27.7")
  testImplementation("com.squareup.okhttp3:mockwebserver:5.3.2")
  testImplementation("org.mockito.kotlin:mockito-kotlin:6.3.0")
  testImplementation(kotlin("reflect"))
}

tasks.test {
  useJUnitPlatform()
}

kotlin {
  jvmToolchain(25)
}
