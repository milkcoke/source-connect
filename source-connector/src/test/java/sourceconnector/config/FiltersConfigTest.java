package sourceconnector.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import sourceconnector.config.FiltersConfig.FilterConfig;
import sourceconnector.config.util.YamlTestUtils;
import sourceconnector.repository.file.filter.FileExcludeFilter;
import sourceconnector.repository.file.filter.FileExtensionFilter;
import sourceconnector.repository.file.filter.FileFilter;
import sourceconnector.repository.file.validator.CompositeFileValidator;
import sourceconnector.repository.file.validator.FileValidator;
import sourceconnector.repository.file.validator.NoConditionFileValidator;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FiltersConfigTest {

  @DisplayName("Should get NoConditionFileValidator when filter are not provided")
  @Test
  void noFileValidatorTest() throws IOException {
    // given
    Map<String, Object> map = YamlTestUtils.getStringObjectMap("""
      app:
        storage:
          type: local
          paths: ['test']
          configs:
            filters:
      """);
    Binder binder = new Binder(new MapConfigurationPropertySource(map));
    FiltersConfig filtersConfig = binder.bind("app.storage.configs", FiltersConfig.class).get();

    // when
    FileValidator fileValidator = filtersConfig.toValidator();
    // then
    assertThat(fileValidator).isInstanceOf(NoConditionFileValidator.class);
  }

  @DisplayName("Should get CompositeFileValidator when filter are provided")
  @Test
  void compositeFileValidatorCreationTest() throws IOException {
    // given
    Map<String, Object> map = YamlTestUtils.getStringObjectMap("""
      app:
        storage:
          type: local
          paths: ['test']
          configs:
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
    FiltersConfig filtersConfig = binder.bind("app.storage.configs", FiltersConfig.class).get();

    // when
    FileValidator fileValidator = filtersConfig.toValidator();
    // then
    assertThat(fileValidator).isInstanceOf(CompositeFileValidator.class);
  }

  @DisplayName("Should create CompositeFileValidator according to type, expressions")
  @Test
  void filterSequenceTest() throws IOException {
    // given
    Map<String, Object> map = YamlTestUtils.getStringObjectMap("""
          configs:
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
    FiltersConfig filtersConfig = binder.bind("configs", FiltersConfig.class).get();
    List<FileFilter> fileFilterList = filtersConfig.filters().stream()
      .map(FilterConfig::toFileFilter)
      .toList();

    // then
    assertThat(fileFilterList)
      .hasSize(2)
      .hasExactlyElementsOfTypes(FileExtensionFilter.class, FileExcludeFilter.class);

  }
}
