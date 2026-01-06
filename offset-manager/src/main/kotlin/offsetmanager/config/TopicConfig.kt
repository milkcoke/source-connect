package offsetmanager.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TopicConfig() {
  @Value("\${app.offset-topic}")
  private lateinit var offsetTopic: String

  // ➜ Register the topic name as a bean
  @Bean("offsetTopicName")
  fun offsetTopicName(): String {
    return offsetTopic
  }
}
