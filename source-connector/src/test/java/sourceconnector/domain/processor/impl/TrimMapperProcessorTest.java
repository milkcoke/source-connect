package sourceconnector.domain.processor.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sourceconnector.domain.log.factory.JSONLogFactory;
import sourceconnector.domain.log.JSONLog;
import sourceconnector.domain.log.Log;
import sourceconnector.domain.log.LogMetadata;
import sourceconnector.domain.processor.impl.TrimMapperProcessor;

import static org.assertj.core.api.Assertions.assertThat;


class TrimMapperProcessorTest {

  @DisplayName("Should trim leading and trailing whitespace from input payload")
  @Test
  void trimWhiteSpaceLeadingAndTrailing() {
    // given
    TrimMapperProcessor processor = new TrimMapperProcessor(new JSONLogFactory());
    Log input = new JSONLog("   test payload   ", LogMetadata.EMPTY);
    // when
    Log result = processor.map(input);
    // then
    assertThat(result.get()).isEqualTo("test payload");
  }

  @DisplayName("Remove all whitespace if the input payload is only whitespace")
  @Test
  void removeAllWhiteSpace() {
    // given
    TrimMapperProcessor processor = new TrimMapperProcessor(new JSONLogFactory());
    Log input = new JSONLog("   ", LogMetadata.EMPTY);
    // when
    Log result = processor.map(input);
    // then
    assertThat(result.get()).isEqualTo("");
  }
}
