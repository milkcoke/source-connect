package sourceconnector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("target.kafka")
public record TopicConfig(
  String offsetTopic,
  String sinkTopic
) {
  public TopicConfig {
    if (offsetTopic == null || offsetTopic.isBlank()) {
      throw new IllegalArgumentException("offsetTopic must not be null or blank");
    }
    if (sinkTopic == null || sinkTopic.isBlank()) {
      throw new IllegalArgumentException("sinkTopic must not be null or blank");
    }
  }
}
