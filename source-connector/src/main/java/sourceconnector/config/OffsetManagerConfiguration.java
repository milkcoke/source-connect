package sourceconnector.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sourceconnector.domain.connect.OffsetRecordService;
import sourceconnector.repository.offset.HttpOffsetRecordRepository;
import sourceconnector.repository.offset.InternalOffsetRecordRepository;
import sourceconnector.service.offset.OffsetRecordRepository;
import sourceconnector.service.offset.OffsetRecordServiceImpl;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

@Slf4j
@Configuration
public class OffsetManagerConfiguration {

  @Bean
  public OffsetRecordRepository offsetRecordRepository(
    OffsetManagerConfig offsetManagerConfig,
    KafkaConsumer<String, Long> kafkaConsumer,
    AdminClient  adminClient,
    TopicConfig topicConfig
  ) {

    String baseUrl = offsetManagerConfig.baseUrl();
    if (baseUrl == null || baseUrl.isEmpty()) {
      log.info("baseUrl is omitted, {} is to be used.", InternalOffsetRecordRepository.class.getSimpleName());
      return new InternalOffsetRecordRepository(kafkaConsumer, adminClient, topicConfig.offsetTopic());
    }

    try {
      URL url = URI.create(offsetManagerConfig.baseUrl()).toURL();
      String protocol = url.getProtocol();

      switch (protocol) {
        case "http", "https" -> {
          return new HttpOffsetRecordRepository(url.toString());
        }
        default -> throw new IllegalArgumentException("Unsupported protocol: " + protocol);
      }
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Invalid baseUrl: " + baseUrl);
    }
  }

  @Bean
  public OffsetRecordService offsetRecordService(OffsetRecordRepository offsetRecordRepository) {
    return new OffsetRecordServiceImpl(offsetRecordRepository);
  }
}
