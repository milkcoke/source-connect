package localoffsetmanager.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class TopicConfig {

  @Bean
  public NewTopic offsetTopic() {
    return TopicBuilder
      .name("s3-offset-topic")
      .partitions(2)
      .replicas(3)
      .compact()
      .config("segment.bytes", "16777216")
      .build();
  }


}
