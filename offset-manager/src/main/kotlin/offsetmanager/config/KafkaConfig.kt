package offsetmanager.config

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.admin.AdminClient
import org.springframework.boot.kafka.autoconfigure.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
class KafkaConfig {
  @Bean
  fun consumerProperties(kafkaProperties: KafkaProperties): Properties {
    val properties = Properties()
    properties[CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG] = kafkaProperties.bootstrapServers.joinToString(separator = ",")
    properties.putAll(kafkaProperties.consumer.buildProperties())
    return properties
  }

  @Bean
  fun adminClient(kafkaProperties: KafkaProperties): AdminClient {
    val properties = Properties()
    properties[CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG] = kafkaProperties.bootstrapServers.joinToString(separator = ",")
    properties.putAll(kafkaProperties.admin.buildProperties())
    return AdminClient.create(properties)
  }
}
