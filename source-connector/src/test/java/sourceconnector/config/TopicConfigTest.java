package sourceconnector.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import sourceconnector.config.util.YamlTestUtils;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TopicConfigTest {

  @DisplayName("Should throw IllegalArgumentException when offset topic is null or empty")
  @Test
  void offsetTopicPropertyMissingTest() {
    assertThatThrownBy(()-> new TopicConfig(" ", "sink-topic"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("offsetTopic must not be null or blank");
  }

  @DisplayName("Should throw IllegalArgumentException when sink topic is null or empty")
  @Test
  void sinkTopicPropertyMissingTest() {
    assertThatThrownBy(()-> new TopicConfig("offset-topic", ""))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("sinkTopic must not be null or blank");
  }

  @DisplayName("Should create CompositeFileValidator according to type, expressions")
  @Test
  void offsetMissingConfigMissingTest() throws IOException {
    // given
    Map<String, Object> map = YamlTestUtils.getStringObjectMap("""
        target:
          kafka:
            offsetTopic:
            sinkTopic: sink-topic
      """);
    Binder binder = new Binder(new MapConfigurationPropertySource(map));
    // when then
    assertThatThrownBy(() -> binder.bind("target.kafka", TopicConfig.class).get())
      .hasRootCauseInstanceOf(IllegalArgumentException.class)
      .hasStackTraceContaining("offsetTopic must not be null or blank");
  }

  @DisplayName("Should create CompositeFileValidator according to type, expressions")
  @Test
  void topicConfigMissingTest() throws IOException {
    // given
    Map<String, Object> map = YamlTestUtils.getStringObjectMap("""
        target:
          kafka:
            offsetTopic: offset-topic
            sinkTopic: 
      """);
    Binder binder = new Binder(new MapConfigurationPropertySource(map));
    // when then
    assertThatThrownBy(() -> binder.bind("target.kafka", TopicConfig.class).get())
      .hasRootCauseInstanceOf(IllegalArgumentException.class)
      .hasStackTraceContaining("sinkTopic must not be null or blank");
  }

  @DisplayName("Should succeed creating TopicConfig when all properties are provided")
  @Test
  void topicConfigTest() throws IOException {
    // given
    Map<String, Object> map = YamlTestUtils.getStringObjectMap("""
        target:
          kafka:
            offsetTopic: offset-topic
            sinkTopic: sink-topic
      """);
    Binder binder = new Binder(new MapConfigurationPropertySource(map));
    // when
    TopicConfig topicConfig =  binder.bind("target.kafka", TopicConfig.class).get();
    // then
    assertThat(topicConfig.offsetTopic()).isEqualTo("offset-topic");
    assertThat(topicConfig.sinkTopic()).isEqualTo("sink-topic");
  }
}
