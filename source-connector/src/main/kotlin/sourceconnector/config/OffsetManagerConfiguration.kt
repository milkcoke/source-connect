package sourceconnector.config

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import sourceconnector.config.OffsetManagerConfig.RepositoryType
import sourceconnector.config.OffsetManagerConfig.RepositoryType.HTTP
import sourceconnector.config.OffsetManagerConfig.RepositoryType.INTERNAL
import sourceconnector.domain.connect.OffsetRecordService
import sourceconnector.repository.offset.HttpOffsetRecordRepository
import sourceconnector.repository.offset.InternalOffsetRecordRepository
import sourceconnector.service.offset.OffsetRecordRepository
import sourceconnector.service.offset.OffsetRecordServiceImpl
import java.net.URI
import java.net.URL

@Configuration
class OffsetManagerConfiguration {
  private val log = LoggerFactory.getLogger(OffsetManagerConfiguration::class.java)
  @Bean
  fun offsetRecordRepository(
    offsetManagerConfig: OffsetManagerConfig,
    kafkaConsumer: KafkaConsumer<String, Long>,
    adminClient: AdminClient,
    topicConfig: TopicConfig
  ): OffsetRecordRepository {
    val type: RepositoryType = offsetManagerConfig.type
    when (type) {
      INTERNAL -> {
        log.info("Using InternalOffsetRecordRepository as offset record repository.")
        return InternalOffsetRecordRepository(kafkaConsumer, adminClient, topicConfig.offsetTopic)
      }

      HTTP -> {
        val baseUrl = offsetManagerConfig.baseUrl
        if (baseUrl.isNullOrEmpty()) {
          throw IllegalArgumentException("baseUrl must be provided for HTTP repository type.")
        }
        if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
          throw IllegalArgumentException("Invalid baseUrl: $baseUrl")
        }

        return HttpOffsetRecordRepository(offsetManagerConfig.baseUrl)
      }
    }
  }


  @Bean
  fun offsetRecordService(offsetRecordRepository: OffsetRecordRepository): OffsetRecordService {
    return OffsetRecordServiceImpl(offsetRecordRepository)
  }
}
