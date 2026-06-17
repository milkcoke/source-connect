plugins {
  kotlin("jvm")
  kotlin("plugin.spring") version "2.4.0"
  kotlin("plugin.allopen") version "2.4.0"
  id("org.springframework.boot") version "4.1.0"
  id("me.champeau.jmh") version "0.7.3"
}

group = "sourceconnect"
version = "1.0.0"

repositories {
  mavenCentral()
}

dependencies {
  implementation(project(":offset-manager-api"))
  implementation(project(":source-connector"))
  implementation("org.apache.kafka:kafka-clients:4.3.0")
  implementation("org.springframework.boot:spring-boot-starter-kafka:4.1.0")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

  testImplementation(platform("org.junit:junit-bom:6.1.0"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("org.mockito.kotlin:mockito-kotlin:6.3.0")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
  useJUnitPlatform()
}

allOpen {
  // JMH requires the benchmark classes to be non-final
  // since it write bytecode at runtime inheriting original benchmark classes.
  annotation("org.openjdk.jmh.annotations.BenchmarkMode")
}

jmh {
  fork.set(1)
  warmupIterations.set(1)
  iterations.set(1)
//  includes.set(listOf("FileSourceTaskBenchmark.singleTaskBenchmark"))
}

kotlin {
  jvmToolchain(25)
}
