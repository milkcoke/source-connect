package sourceconnector.config;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ByteArrayResource;
import sourceconnector.config.StorageConfig.StorageType;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StorageConfigTest {

  @DisplayName("Should get storage mapping according to yaml string")
  @Test
  void storageConfigMappingTest() throws IOException {
    // given
    Map<String, Object> map = getStringObjectMap("""
    app:
      storage:
        type: s3
        paths:
          - s3://my-bucket/foo
          - s3://my-bucket/bar
    """);

    Binder binder = new Binder(new MapConfigurationPropertySource(map));
    // when
    StorageConfig config = binder.bind("app.storage", StorageConfig.class).get();

    // then
    assertThat(config.type()).isEqualTo(StorageType.S3);
    assertThat(config.filters()).isNullOrEmpty();
    assertThat(config.paths()).containsExactlyInAnyOrder(
      "s3://my-bucket/foo",
      "s3://my-bucket/bar"
    );
  }

  @SuppressWarnings("unchecked")
  @NotNull
  private Map<String, Object> getStringObjectMap(String yamlStr) throws IOException {

    var loader = new YamlPropertySourceLoader();
    var resource = new ByteArrayResource(yamlStr.getBytes());
    var propertySource = loader.load("test", resource).getFirst();

    // Convert PropertySource to MapConfigurationPropertySource for Binder
    return (Map<String, Object>) propertySource.getSource();
  }
}
