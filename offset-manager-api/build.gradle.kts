plugins {
  java
  kotlin("jvm")
}

group = "sourceconnect"
version = "0.9.3"

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

  testImplementation(platform("org.junit:junit-bom:6.0.2"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("org.assertj:assertj-core:3.27.6")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
  useJUnitPlatform()
}

kotlin {
  jvmToolchain(21)
}
