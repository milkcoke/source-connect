package sourceconnector.service.processor.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sourceconnector.domain.log.JSONLog;
import sourceconnector.domain.log.Log;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class EmptyFilterProcessorTest {

  @DisplayName("Should result is to be null when payload is empty")
  @Test
  void shouldReturnFalseForEmptyOrBlankInput() {
    // given
    EmptyFilterProcessor processor = new EmptyFilterProcessor();
    JSONLog emptyInput = new JSONLog("", null);

    // when
    Log result = processor.process(emptyInput);

    // then
    assertThat(result).isNull();
  }
}
