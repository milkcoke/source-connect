plugins {
  java
  kotlin("jvm")
}

group = "sourceconnect"
version = "1.0.0"

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

  testImplementation(platform("org.junit:junit-bom:6.0.3"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("org.assertj:assertj-core:3.27.7")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
  useJUnitPlatform()
}

kotlin {
  jvmToolchain(25)
}
