package sourceconnector.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import sourceconnector.config.util.YamlTestUtils;
import sourceconnector.domain.log.Log;
import sourceconnector.service.processor.BaseProcessor;
import sourceconnector.service.processor.impl.ByPassProcessor;
import sourceconnector.service.processor.impl.EmptyFilterProcessor;
import sourceconnector.service.processor.impl.TrimMapperProcessor;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PipelineConfigTest {

  @DisplayName("Should get ByPassProcessor when pipeline is not provided")
  @Test
  void ByPassProcessorTest() throws IOException {
    // given
    Map<String, Object> map = YamlTestUtils.getStringObjectMap("""
      transform:
        pipeline:
      """);
    Binder binder = new Binder(new MapConfigurationPropertySource(map));
    PipelineConfig pipelineConfig = binder.bind("transform", PipelineConfig.class).get();

    // when
    List<BaseProcessor<Log>> processorList = pipelineConfig.toProcessors();
    // then
    assertThat(processorList)
      .hasExactlyElementsOfTypes(ByPassProcessor.class);
  }

  @DisplayName("Should get two processors when pipeline consists of two processors")
  @Test
  void twoProcessorsTest() throws IOException {
    // given
    Map<String, Object> map = YamlTestUtils.getStringObjectMap("""
      transform:
        pipeline:
          - type: Trim
          - type: SkipBlank
      """);
    Binder binder = new Binder(new MapConfigurationPropertySource(map));
    PipelineConfig pipelineConfig = binder.bind("transform", PipelineConfig.class).get();

    // when
    List<BaseProcessor<Log>> processorList = pipelineConfig.toProcessors();
    // then
    assertThat(processorList)
      .hasExactlyElementsOfTypes(TrimMapperProcessor.class, EmptyFilterProcessor.class);
  }

  @DisplayName("Should get processor irrelevant to case")
  @Test
  void ignoreTypeCaseTest() throws IOException {
    // given
    Map<String, Object> map = YamlTestUtils.getStringObjectMap("""
      transform:
        pipeline:
          - type: tRIM
          - type: skipBlank
      """);
    Binder binder = new Binder(new MapConfigurationPropertySource(map));
    PipelineConfig pipelineConfig = binder.bind("transform", PipelineConfig.class).get();

    // when
    List<BaseProcessor<Log>> processorList = pipelineConfig.toProcessors();
    // then
    assertThat(processorList)
      .hasExactlyElementsOfTypes(TrimMapperProcessor.class, EmptyFilterProcessor.class);
  }

  @DisplayName("Should throw IllegalArgumentException when incorrect case")
  @Test
  void caseSensitiveTypeTest() throws IOException {
    // given
    Map<String, Object> map = YamlTestUtils.getStringObjectMap("""
      transform:
        pipeline:
          - type: NotExist
      """);
    Binder binder = new Binder(new MapConfigurationPropertySource(map));
    PipelineConfig pipelineConfig = binder.bind("transform", PipelineConfig.class).get();

    // when then
    assertThatThrownBy(pipelineConfig::toProcessors)
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Invalid processor type: NotExist");
  }
}
