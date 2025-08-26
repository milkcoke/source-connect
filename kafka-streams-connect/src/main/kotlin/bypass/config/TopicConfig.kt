package bypass.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app.topic")
data class TopicConfig(
  val sourceTopic: String,
  val sinkTopic: String
)
