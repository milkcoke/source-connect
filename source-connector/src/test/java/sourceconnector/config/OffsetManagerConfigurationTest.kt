package sourceconnector.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import sourceconnector.repository.offset.HttpOffsetRecordRepository;
import sourceconnector.repository.offset.InternalOffsetRecordRepository;
import sourceconnector.service.offset.OffsetRecordRepository;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;

class OffsetManagerConfigurationTest {
  static Stream<Arguments> baseUrls() {
    return Stream.of(
      arguments(null, InternalOffsetRecordRepository.class),
      arguments("", InternalOffsetRecordRepository.class),
      arguments("http://localhost:8080", HttpOffsetRecordRepository.class),
      arguments("https://localhost:8080", HttpOffsetRecordRepository.class)
    );
  }

  @ParameterizedTest
  @MethodSource("baseUrls")
  void offsetRepositorySelectTest(String baseUrl, Class<? extends OffsetRecordRepository> expectedType) {
    OffsetManagerConfig config = new OffsetManagerConfig(baseUrl);
    OffsetManagerConfiguration configuration = new OffsetManagerConfiguration();
    OffsetRecordRepository repository = configuration.offsetRecordRepository(
      config,
      mock(KafkaConsumer.class),
      mock(AdminClient.class),
      mock(TopicConfig.class)
    );

    assertThat(repository).isInstanceOf(expectedType);
  }

  @DisplayName("Should throw IllegalArgumentException when invalid url format baseUrl is provided")
  @Test
  void invalidUrlTest() {
    // given
    OffsetManagerConfiguration offsetManagerConfiguration = new OffsetManagerConfiguration();
    OffsetManagerConfig config = new OffsetManagerConfig("localhost:8080");
    // when then
    assertThatThrownBy(()-> offsetManagerConfiguration.offsetRecordRepository(
      config,
      mock(KafkaConsumer.class),
      mock(AdminClient.class),
      mock(TopicConfig.class)
    ))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Invalid baseUrl: localhost:8080");
  }
}
