package sourceconnector.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import sourceconnector.config.util.YamlTestUtils;
import sourceconnector.repository.file.validator.CompositeFileValidator;
import sourceconnector.repository.file.validator.FileValidator;
import sourceconnector.repository.file.validator.NoConditionFileValidator;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileSearchConfigsTest {

  @DisplayName("Should throw BindException when missing recursive option")
  @Test
  void recursiveOptionMissingTest() throws IOException {
    // given
    Map<String, Object> map = YamlTestUtils.getStringObjectMap("""
      source:
        storage:
          type: local
          paths: ['test']
          configs:
            recursive:
      """);
    Binder binder = new Binder(new MapConfigurationPropertySource(map));
    // when then
    assertThatThrownBy(()-> binder.bind("source.storage.configs", FileSearchConfigs.class).get())
      .isInstanceOf(BindException.class);
  }

  @DisplayName("Should get recursive option correctly")
  @Test
  void recursiveParseTest() throws IOException {
    // given
    Map<String, Object> map = YamlTestUtils.getStringObjectMap("""
      source:
        storage:
          type: local
          paths: ['test']
          configs:
            recursive: true
            filters:
      """);
    Binder binder = new Binder(new MapConfigurationPropertySource(map));

    // when
    FileSearchConfigs configs = binder.bind("source.storage.configs", FileSearchConfigs.class).get();
    // then
    assertThat(configs.isRecursive()).isTrue();
  }

  @DisplayName("Should get NoConditionFileValidator when filter are not provided")
  @Test
  void noFileValidatorTest() throws IOException {
    // given
    Map<String, Object> map = YamlTestUtils.getStringObjectMap("""
      source:
        storage:
          type: local
          paths: ['test']
          configs:
            recursive: true
            filters:
      """);
    Binder binder = new Binder(new MapConfigurationPropertySource(map));
    FileSearchConfigs configs = binder.bind("source.storage.configs", FileSearchConfigs.class).get();

    // when
    FileValidator fileValidator = configs.toValidator();
    // then
    assertThat(fileValidator).isInstanceOf(NoConditionFileValidator.class);
  }

  @DisplayName("Should get CompositeFileValidator when filter are provided")
  @Test
  void compositeFileValidatorCreationTest() throws IOException {
    // given
    Map<String, Object> map = YamlTestUtils.getStringObjectMap("""
      source:
        storage:
          type: local
          paths: ['test']
          configs:
            recursive: false
            filters:
              - type: extension
                expressions:
                  - '.csv'
                  - '.ndjson'
              - type: exclude
                expressions:
                  - ".*tmp.*"
      """);
    Binder binder = new Binder(new MapConfigurationPropertySource(map));
    FileSearchConfigs configs = binder.bind("source.storage.configs", FileSearchConfigs.class).get();

    // when
    FileValidator fileValidator = configs.toValidator();
    // then
    assertThat(fileValidator).isInstanceOf(CompositeFileValidator.class);
  }


  @DisplayName("Get recursive and filters both")
  @Test
  void FileSearchConfigMappingTest() throws IOException {
    // given
    Map<String, Object> map = YamlTestUtils.getStringObjectMap("""
      source:
        storage:
          type: local
          paths: ['test']
          configs:
            recursive: true
            filters:
              - type: exclude
                expressions:
                  - ".*tmp.*"
      """);
    Binder binder = new Binder(new MapConfigurationPropertySource(map));

    // when
    FileSearchConfigs configs = binder.bind("source.storage.configs", FileSearchConfigs.class).get();
    // then
    assertThat(configs.isRecursive()).isTrue();
    assertThat(configs.toValidator()).isInstanceOf(CompositeFileValidator.class);

  }
}
