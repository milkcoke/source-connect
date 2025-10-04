package sourceconnector.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KafkaConfig {
  @Value("${spring.kafka.consumer.max-poll-records}")
  private int maxPollRecords;

  @Bean
  public Properties produerProperties(KafkaProperties kafkaProperties) {
    Properties properties = new Properties();
    properties.putAll(kafkaProperties.getProducer().buildProperties(null));
    return properties;
  }

  @Bean
  public KafkaConsumer<String, Long> consumer(KafkaProperties kafkaProperties) {
    Properties properties = new Properties();
    properties.putAll(kafkaProperties.getConsumer().buildProperties(null));
    return new KafkaConsumer<>(properties);
  }

  @Bean
  public AdminClient adminClient(KafkaProperties kafkaProperties) {
    Properties properties = new Properties();
    properties.putAll(kafkaProperties.getAdmin().buildProperties(null));
    return AdminClient.create(properties);
  }


}
