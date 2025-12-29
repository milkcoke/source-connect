package sourceconnector.config

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.ThrowableAssert
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import sourceconnector.repository.offset.HttpOffsetRecordRepository
import sourceconnector.repository.offset.InternalOffsetRecordRepository
import sourceconnector.service.offset.OffsetRecordRepository
import java.util.stream.Stream

internal class OffsetManagerConfigurationTest {
  @ParameterizedTest
  @MethodSource("baseUrls")
  fun offsetRepositorySelectTest(baseUrl: String, expectedType: Class<out OffsetRecordRepository>) {
    val config = OffsetManagerConfig(baseUrl)
    val configuration = OffsetManagerConfiguration()
    val repository = configuration.offsetRecordRepository(
      config,
      Mockito.mock<KafkaConsumer<*, *>>(KafkaConsumer::class.java),
      Mockito.mock<AdminClient>(AdminClient::class.java),
      Mockito.mock<TopicConfig>(TopicConfig::class.java)!!
    )

    Assertions.assertThat<OffsetRecordRepository?>(repository).isInstanceOf(expectedType)
  }

  @DisplayName("Should throw IllegalArgumentException when invalid url format baseUrl is provided")
  @Test
  fun invalidUrlTest() {
    // given
    val offsetManagerConfiguration = OffsetManagerConfiguration()
    val config = OffsetManagerConfig(OffsetManagerConfig.RepositoryType.HTTP, null)
    // when then
    assertThatThrownBy {
      offsetManagerConfiguration.offsetRecordRepository(
        config,
        Mockito.mock<KafkaConsumer<*, *>>(KafkaConsumer::class.java),
        Mockito.mock<AdminClient>(AdminClient::class.java),
        Mockito.mock<TopicConfig>(TopicConfig::class.java)!!
      )
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("Invalid baseUrl: localhost:8080")
  }

  companion object {
    @JvmStatic
    fun baseUrls(): Stream<Arguments?> {
      return Stream.of<Arguments?>(
        Arguments.arguments(null, InternalOffsetRecordRepository::class.java),
        Arguments.arguments("", InternalOffsetRecordRepository::class.java),
        Arguments.arguments("http://localhost:8080", HttpOffsetRecordRepository::class.java),
        Arguments.arguments("https://localhost:8080", HttpOffsetRecordRepository::class.java)
      )
    }
  }
}
