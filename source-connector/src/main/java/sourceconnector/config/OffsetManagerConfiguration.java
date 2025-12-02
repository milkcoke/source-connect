package sourceconnector.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sourceconnector.domain.connect.OffsetRecordService;
import sourceconnector.repository.offset.HttpOffsetRecordRepository;
import sourceconnector.repository.offset.InternalOffsetRecordRepository;
import sourceconnector.service.offset.OffsetRecordRepository;
import sourceconnector.service.offset.OffsetRecordServiceImpl;

import java.net.URL;

@Configuration
public class OffsetManagerConfiguration {

  @Bean
  public OffsetRecordRepository offsetRecordRepository(
    OffsetManagerConfig offsetManagerConfig,
    KafkaConsumer<String, Long> kafkaConsumer,
    AdminClient  adminClient,
    TopicConfig topicConfig
  ) {

    // TODO: Refactor this
    URL url = offsetManagerConfig.baseUrl();
    if (url == null) {
      return new InternalOffsetRecordRepository(kafkaConsumer, adminClient, topicConfig.offsetTopic());
    }

    String protocol = url.getProtocol();

    switch (protocol) {
      case "http", "https" -> {
         return new HttpOffsetRecordRepository(url.toString());
      }
      default -> throw new IllegalArgumentException("Unknown protocol: " + protocol);
    }

  }

  @Bean
  public OffsetRecordService offsetRecordService(OffsetRecordRepository offsetRecordRepository) {
    return new OffsetRecordServiceImpl(offsetRecordRepository);
  }
}
