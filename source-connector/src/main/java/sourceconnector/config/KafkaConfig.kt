package sourceconnector.config

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.springframework.boot.kafka.autoconfigure.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*
import kotlin.Long
import kotlin.arrayOf

@Configuration
class KafkaConfig {
  @Bean(name = ["producerProperties"])
  fun produerProperties(kafkaProperties: KafkaProperties): Properties {
    val properties = Properties()
    properties.put(
      CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG,
      kafkaProperties.bootstrapServers.joinToString(",")
    )
    properties.putAll(kafkaProperties.getProducer().buildProperties())
    return properties
  }

  @Bean
  fun consumer(kafkaProperties: KafkaProperties): KafkaConsumer<kotlin.String?, Long?> {
    val properties = Properties()
    properties.put(
      CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG,
      kafkaProperties.bootstrapServers.joinToString(",")
    )
    properties.putAll(kafkaProperties.getConsumer().buildProperties())
    return KafkaConsumer<kotlin.String?, Long?>(properties)
  }

  @Bean
  fun adminClient(kafkaProperties: KafkaProperties): AdminClient? {
    val properties = Properties()
    properties.put(
      CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG,
      kafkaProperties.bootstrapServers.joinToString(",")
    )
    properties.putAll(kafkaProperties.getAdmin().buildProperties())
    return AdminClient.create(properties)
  }
}
