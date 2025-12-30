package sourceconnector.config

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.mock
import sourceconnector.config.OffsetManagerConfig.RepositoryType
import sourceconnector.repository.offset.HttpOffsetRecordRepository
import sourceconnector.repository.offset.InternalOffsetRecordRepository
import sourceconnector.service.offset.OffsetRecordRepository
import java.util.stream.Stream

internal class OffsetManagerConfigurationTest {
  private val mockConsumer: KafkaConsumer<String, Long> = mock<KafkaConsumer<String, Long>>()
  private val mockAdminClient: AdminClient = mock<AdminClient>()
  private val topicConfig: TopicConfig = TopicConfig("test-offset", "test-sink")

  @ParameterizedTest
  @MethodSource("baseUrls")
  fun offsetRepositorySelectTest(baseUrl: String, type: RepositoryType, expectedType: Class<out OffsetRecordRepository>) {
    val config = OffsetManagerConfig(type, baseUrl)
    val configuration = OffsetManagerConfiguration()
    val repository = configuration.offsetRecordRepository(
      config,
      mockConsumer,
      mockAdminClient,
      topicConfig
    )

    Assertions.assertThat<OffsetRecordRepository?>(repository).isInstanceOf(expectedType)
  }

  @DisplayName("Should throw IllegalArgumentException when invalid url format baseUrl is provided")
  @Test
  fun invalidUrlTest() {
    // given
    val offsetManagerConfiguration = OffsetManagerConfiguration()
    val config = OffsetManagerConfig(RepositoryType.HTTP, "localhost:8080")
    // when then
    assertThatThrownBy {
      offsetManagerConfiguration.offsetRecordRepository(
        config,
        mockConsumer,
        mockAdminClient,
        topicConfig
      )
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("Invalid baseUrl: localhost:8080")
  }

  companion object {
    @JvmStatic
    fun baseUrls(): Stream<Arguments?> {
      return Stream.of<Arguments?>(
        Arguments.arguments("", RepositoryType.INTERNAL, InternalOffsetRecordRepository::class.java),
        Arguments.arguments("", RepositoryType.INTERNAL, InternalOffsetRecordRepository::class.java),
        Arguments.arguments("http://localhost:8080", RepositoryType.HTTP, HttpOffsetRecordRepository::class.java),
        Arguments.arguments("https://localhost:8080", RepositoryType.HTTP, HttpOffsetRecordRepository::class.java)
      )
    }
  }
}
