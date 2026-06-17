import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm")
  kotlin("plugin.spring") version "2.4.0"
  id("org.springframework.boot") version "4.1.0"
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

  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-kafka")
  implementation("org.apache.kafka:kafka-clients:4.3.0")
  implementation("tools.jackson.module:jackson-module-kotlin:3.2.0")
  implementation("org.awaitility:awaitility:4.3.0")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")


  testImplementation(platform("org.junit:junit-bom:6.1.0"))
  testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
  testImplementation(platform("org.testcontainers:testcontainers-bom:2.0.5"))
  testImplementation("org.testcontainers:testcontainers-junit-jupiter")
  testImplementation("org.testcontainers:testcontainers-kafka")
  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("org.assertj:assertj-core:3.27.7")
  testImplementation("org.mockito:mockito-core:5.23.0")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}

tasks.test {
  useJUnitPlatform()
}

kotlin {
    jvmToolchain(25)
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
  freeCompilerArgs.set(listOf("-Xannotation-default-target=param-property=param-property"))
}
