package sourceconnector.config

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource
import sourceconnector.config.util.YamlTestUtils.getStringObjectMap
import sourceconnector.domain.log.Log
import sourceconnector.domain.processor.BaseProcessor
import sourceconnector.domain.processor.impl.EmptyFilterProcessor
import sourceconnector.domain.processor.impl.TrimMapperProcessor
import java.io.IOException

internal class PipelineConfigTest {
  @DisplayName("Should get empty list when no processor")
  @Test
  @Throws(IOException::class)
  fun byPassProcessorTest() {
    // given
    val map: Map<String, Any> = getStringObjectMap(
      """
      processing:
        pipelines:
      """.trimIndent()
    )
    val binder = Binder(MapConfigurationPropertySource(map))
    val pipelineConfig = binder.bind<PipelineConfig>("processing", PipelineConfig::class.java).get()

    // when
    val processorList: List<BaseProcessor<Log>> = pipelineConfig.toProcessors()
    // then
    assertThat<BaseProcessor<Log>>(processorList).isEmpty()
  }

  @DisplayName("Should get two processors when pipeline consists of two processors")
  @Test
  @Throws(IOException::class)
  fun twoProcessorsTest() {
    // given
    val map: Map<String, Any> = getStringObjectMap(
      """
      processing:
        pipelines:
          - type: Trim
          - type: SkipBlank
      """.trimIndent()
    )
    val binder = Binder(MapConfigurationPropertySource(map))
    val pipelineConfig = binder.bind<PipelineConfig>("processing", PipelineConfig::class.java).get()

    // when
    val processorList: List<BaseProcessor<Log>> = pipelineConfig.toProcessors()
    // then
    assertThat<BaseProcessor<Log>>(processorList)
      .hasExactlyElementsOfTypes(
        TrimMapperProcessor::class.java,
        EmptyFilterProcessor::class.java
      )
  }

  @DisplayName("Should get processor irrelevant to case")
  @Test
  @Throws(IOException::class)
  fun ignoreTypeCaseTest() {
    // given
    val map: Map<String, Any> = getStringObjectMap(
      """
      processing:
        pipelines:
          - type: tRIM
          - type: skipBlank
      """.trimIndent()
    )
    val binder = Binder(MapConfigurationPropertySource(map))
    val pipelineConfig = binder.bind<PipelineConfig>("processing", PipelineConfig::class.java).get()

    // when
    val processorList: List<BaseProcessor<Log>> = pipelineConfig.toProcessors()
    // then
    assertThat<BaseProcessor<Log>>(processorList)
      .hasExactlyElementsOfTypes(
        TrimMapperProcessor::class.java,
        EmptyFilterProcessor::class.java
      )
  }

  @DisplayName("Should throw IllegalArgumentException when incorrect case")
  @Test
  @Throws(IOException::class)
  fun caseSensitiveTypeTest() {
    // given
    val map: Map<String, Any> = getStringObjectMap(
      """
      processing:
        pipelines:
          - type: NotExist
      """.trimIndent()
    )
    val binder = Binder(MapConfigurationPropertySource(map))
    val pipelineConfig = binder.bind<PipelineConfig>("processing", PipelineConfig::class.java).get()

    // when then
    assertThatThrownBy { pipelineConfig.toProcessors() }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("Invalid processor type: NotExist")
  }
}
