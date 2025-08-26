package bypass.config

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsConfig
import org.springframework.context.annotation.Configuration
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.kafka.annotation.EnableKafkaStreams
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration
import org.springframework.kafka.config.KafkaStreamsConfiguration

@Configuration
@EnableKafkaStreams
class KafkaConfig {

    @Bean(KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    fun kafkaStreamsConfig(kafkaProperties: KafkaProperties): KafkaStreamsConfiguration {
        val producerProperties = kafkaProperties.buildProducerProperties(null)
        val consumerProperties = kafkaProperties.buildConsumerProperties(null)
        val streamsConfig = kafkaProperties.buildStreamsProperties(null)

        streamsConfig.putAll(producerProperties)
        streamsConfig.putAll(consumerProperties)

        val config = mapOf(
            StreamsConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaProperties.bootstrapServers,
            StreamsConfig.APPLICATION_ID_CONFIG to streamsConfig.get(StreamsConfig.APPLICATION_ID_CONFIG),
            StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG to  Serdes.ByteArray().javaClass.name,
            StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG to Serdes.String().javaClass.name,
            StreamsConfig.NUM_STREAM_THREADS_CONFIG to 1,
            "internal.leave.group.on.close" to true,
            StreamsConfig.consumerPrefix(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG) to consumerProperties.get(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG),
            StreamsConfig.consumerPrefix(SaslConfigs.SASL_MECHANISM) to consumerProperties.get(SaslConfigs.SASL_MECHANISM),

            StreamsConfig.producerPrefix(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG) to consumerProperties.get(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG),
            StreamsConfig.producerPrefix(SaslConfigs.SASL_MECHANISM) to producerProperties.get(SaslConfigs.SASL_MECHANISM),
        )
        streamsConfig.putAll(config)

        return KafkaStreamsConfiguration(streamsConfig)
    }
}
