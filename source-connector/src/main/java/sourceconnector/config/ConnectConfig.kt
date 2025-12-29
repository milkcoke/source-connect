package sourceconnector.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "connect")
data class ConnectConfig(
  val workerCount: Int,
  val taskCount: Int
) {
  init {
    require(workerCount >= 1) { "workerCount must be >= 1" }
    require(taskCount >= workerCount) { "taskCount must be >= workerCount" }
  }
}
