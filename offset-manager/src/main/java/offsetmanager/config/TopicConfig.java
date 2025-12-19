package offsetmanager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TopicConfig {

  @Value("${app.offset-topic}")
  private String offsetTopic;

  // ➜ Register the topic name as a bean
  @Bean("offsetTopicName")
  public String offsetTopicName() {
    return offsetTopic;
  }

}
