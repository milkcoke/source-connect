package offsetmanager.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class TopicConfig {

  @Value("${app.offset-topic}")
  private String offsetTopic;

  // ➜ Register the topic name as a bean
  @Bean("offsetTopicName")
  public String offsetTopicName() {
    return offsetTopic;
  }

  @Bean
  public NewTopic offsetTopic() {
    return TopicBuilder
      .name(offsetTopic)
      .partitions(2)
      .replicas(3)
      .compact()
      .config("segment.bytes", "16777216")
      .build();
  }


}
