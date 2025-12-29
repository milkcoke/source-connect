package sourceconnector.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("target.kafka")
data class TopicConfig(
  val offsetTopic: String,
  val sinkTopic: String
) {
  init {
    require(offsetTopic.isNotBlank()) { "offsetTopic must not be null or blank" }
    require(sinkTopic.isNotBlank()) { "sinkTopic must not be null or blank" }
  }
}
