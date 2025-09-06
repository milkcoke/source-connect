package sourceconnector.service.processor.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sourceconnector.domain.log.JSONLog;

import static org.assertj.core.api.Assertions.assertThat;


class TrimMapperProcessorTest {

  @DisplayName("Should trim leading and trailing whitespace from input payload")
  @Test
  void trimWhiteSpaceLeadingAndTrailing() {
    // given
    TrimMapperProcessor processor = new TrimMapperProcessor();
    JSONLog input = new JSONLog("   test payload   ", null);
    // when
    JSONLog result = processor.map(input);
    // then
    assertThat(result.get()).isEqualTo("test payload");
  }

  @DisplayName("Remove all whitespace if the input payload is only whitespace")
  @Test
  void removeAllWhiteSpace() {
    // given
    TrimMapperProcessor processor = new TrimMapperProcessor();
    JSONLog input = new JSONLog("   ", null);
    // when
    JSONLog result = processor.map(input);
    // then
    assertThat(result.get()).isEqualTo("");
  }
}
