package offsetmanager.config;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KafkaConfig {

  @Bean
  public Properties consumerProperties(KafkaProperties kafkaProperties) {
    Properties properties = new Properties();
    properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, String.join(",", kafkaProperties.getBootstrapServers()));
    properties.putAll(kafkaProperties.getConsumer().buildProperties());
    return properties;
  }

  @Bean
  public AdminClient adminClient(KafkaProperties kafkaProperties) {
    Properties properties = new Properties();
    properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, String.join(",", kafkaProperties.getBootstrapServers()));
    properties.putAll(kafkaProperties.getAdmin().buildProperties());
    return AdminClient.create(properties);
  }
}
