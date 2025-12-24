package sourceconnector.domain.processor.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sourceconnector.domain.log.JSONLog;
import sourceconnector.domain.log.Log;
import sourceconnector.domain.log.LogMetadata;
import sourceconnector.domain.processor.impl.EmptyFilterProcessor;

import static org.assertj.core.api.Assertions.assertThat;

class EmptyFilterProcessorTest {

  @DisplayName("Should result is to be null when payload is empty")
  @Test
  void shouldReturnFalseForEmptyInput() {
    // given
    EmptyFilterProcessor processor = new EmptyFilterProcessor();
    Log emptyInput = new JSONLog("", LogMetadata.EMPTY);

    // when
    Log result = processor.process(emptyInput);

    // then
    assertThat(result).isNull();
  }

  @DisplayName("Should result is to be null when payload is blank")
  @Test
  void shouldReturnFalseForBlankInput() {
    // given
    EmptyFilterProcessor processor = new EmptyFilterProcessor();
    Log emptyInput = new JSONLog("  ", LogMetadata.EMPTY);

    // when
    Log result = processor.process(emptyInput);

    // then
    assertThat(result).isNull();
  }
}
