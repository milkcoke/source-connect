package sourceconnector.config

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.ThrowableAssert
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource
import sourceconnector.config.util.YamlTestUtils.getStringObjectMap
import java.io.IOException

internal class TopicConfigTest {
  @DisplayName("Should throw IllegalArgumentException when offset topic is null or empty")
  @Test
  fun offsetTopicPropertyMissingTest() {
    assertThatThrownBy { TopicConfig(" ", "sink-topic") }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("offsetTopic must not be null or blank")
  }

  @DisplayName("Should throw IllegalArgumentException when sink topic is null or empty")
  @Test
  fun sinkTopicPropertyMissingTest() {
    assertThatThrownBy(ThrowableAssert.ThrowingCallable { TopicConfig("offset-topic", "") })
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("sinkTopic must not be null or blank")
  }

  @DisplayName("Should create CompositeFileValidator according to type, expressions")
  @Test
  @Throws(IOException::class)
  fun offsetMissingConfigMissingTest() {
    // given
    val map: Map<String, Any> = getStringObjectMap(
      """
        target:
          kafka:
            offsetTopic:
            sinkTopic: sink-topic
      """.trimIndent()
    )
    val binder = Binder(MapConfigurationPropertySource(map))
    // when then
    assertThatThrownBy {
      binder.bind<TopicConfig?>(
        "target.kafka",
        TopicConfig::class.java
      ).get()
    }
      .hasRootCauseInstanceOf(IllegalArgumentException::class.java)
      .hasStackTraceContaining("offsetTopic must not be null or blank")
  }

  @DisplayName("Should create CompositeFileValidator according to type, expressions")
  @Test
  @Throws(IOException::class)
  fun topicConfigMissingTest() {
    // given
    val map: Map<String, Any> = getStringObjectMap(
      """
        target:
          kafka:
            offsetTopic: offset-topic
            sinkTopic: 
      """.trimIndent()
    )
    val binder = Binder(MapConfigurationPropertySource(map))
    // when then
    assertThatThrownBy {
      binder.bind<TopicConfig?>(
        "target.kafka",
        TopicConfig::class.java
      ).get()
    }
      .hasRootCauseInstanceOf(IllegalArgumentException::class.java)
      .hasStackTraceContaining("sinkTopic must not be null or blank")
  }

  @DisplayName("Should succeed creating TopicConfig when all properties are provided")
  @Test
  @Throws(IOException::class)
  fun topicConfigTest() {
    // given
    val map: Map<String, Any> = getStringObjectMap(
      """
        target:
          kafka:
            offsetTopic: offset-topic
            sinkTopic: sink-topic
      """.trimIndent()
    )
    val binder = Binder(MapConfigurationPropertySource(map))
    // when
    val topicConfig = binder.bind<TopicConfig>("target.kafka", TopicConfig::class.java).get()
    // then
    Assertions.assertThat(topicConfig.offsetTopic).isEqualTo("offset-topic")
    Assertions.assertThat(topicConfig.sinkTopic).isEqualTo("sink-topic")
  }
}
