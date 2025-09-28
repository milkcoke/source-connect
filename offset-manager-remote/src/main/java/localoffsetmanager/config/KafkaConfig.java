package localoffsetmanager.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KafkaConfig {

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
