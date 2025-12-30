package sourceconnector.domain.processor.impl

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import sourceconnector.domain.log.JSONLog
import sourceconnector.domain.log.Log
import sourceconnector.domain.log.LogMetadata
import sourceconnector.domain.log.factory.JSONLogFactory
import sourceconnector.domain.processor.AbstractMapperProcessor

internal class TrimMapperProcessorTest {
  @DisplayName("Should trim leading and trailing whitespace from input payload")
  @Test
  fun trimWhiteSpaceLeadingAndTrailing() {
    // given
    val processor: AbstractMapperProcessor<Log> = TrimMapperProcessor(JSONLogFactory())
    val input: Log = JSONLog("   test payload   ", LogMetadata.EMPTY)
    // when
    val result = processor.map(input)
    // then
    Assertions.assertThat(result.get()).isEqualTo("test payload")
  }

  @DisplayName("Remove all whitespace if the input payload is only whitespace")
  @Test
  fun removeAllWhiteSpace() {
    // given
    val processor = TrimMapperProcessor(JSONLogFactory())
    val input: Log = JSONLog("   ", LogMetadata.EMPTY)
    // when
    val result: Log = processor.map(input)
    // then
    Assertions.assertThat(result.get()).isEqualTo("")
  }
}
