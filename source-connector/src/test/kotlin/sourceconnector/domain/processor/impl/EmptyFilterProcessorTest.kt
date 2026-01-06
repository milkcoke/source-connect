package sourceconnector.domain.processor.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import sourceconnector.domain.log.JSONLog
import sourceconnector.domain.log.Log
import sourceconnector.domain.log.LogMetadata

internal class EmptyFilterProcessorTest {
  @DisplayName("Should result is to be null when payload is empty")
  @Test
  fun shouldReturnFalseForEmptyInput() {
    // given
    val processor = EmptyFilterProcessor()
    val emptyInput: Log = JSONLog("", LogMetadata.EMPTY)

    // when
    val result = processor.process(emptyInput)

    // then
    assertThat<Log?>(result).isNull()
  }

  @DisplayName("Should result is to be null when payload is blank")
  @Test
  fun shouldReturnFalseForBlankInput() {
    // given
    val processor = EmptyFilterProcessor()
    val emptyInput: Log = JSONLog("  ", LogMetadata.EMPTY)

    // when
    val result = processor.process(emptyInput)

    // then
    assertThat<Log?>(result).isNull()
  }
}
