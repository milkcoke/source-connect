package bypass.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaTopicConfig(
  private val topicConfig: TopicConfig
) {
  @Bean
  fun sourceTopic(): NewTopic {
    return TopicBuilder
      .name(topicConfig.sourceTopic)
      .partitions(1)
      .replicas(2)
      .config("min.insync.replicas", "2")
      .build()
  }

  @Bean
  fun sinkTopic(): NewTopic {
    return TopicBuilder
      .name(topicConfig.sinkTopic)
      .partitions(3)
      .replicas(2)
      .config("min.insync.replicas", "2")
      .config("message.timestamp.type", "LogAppendTime")
      .build()
  }
}
