package sourceconnector.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import sourceconnector.config.util.YamlTestUtils;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class OffsetManagerConfigTest {


  @DisplayName("baseUrl omission is allowed")
  @Test
  void yamlConfigurationTest() throws IOException {
    // FIXME: How to allow baseUrl omission?
    // given
    Map<String, Object> map = YamlTestUtils.getStringObjectMap("""
      offsetManager:
      """);
    Binder binder = new Binder(new MapConfigurationPropertySource(map));

    // when
    OffsetManagerConfig offsetManagerConfig = binder.bind("offset-manager", OffsetManagerConfig.class).get();
    // then
    assertThat(offsetManagerConfig.baseUrl()).isNullOrEmpty();
  }

  @DisplayName("offsetManagerBaseUrl should be parsed as URL")
  @Test
  void urlParseTest() throws IOException {
    // given
    Map<String, Object> map = YamlTestUtils.getStringObjectMap("""
      offsetManager:
        baseUrl: http://localhost:8080
      """);
    Binder binder = new Binder(new MapConfigurationPropertySource(map));
    // when
    OffsetManagerConfig offsetManagerConfig = binder.bind("offset-manager", OffsetManagerConfig.class).get();

    // then
    assertThat(offsetManagerConfig.baseUrl()).isEqualTo("http://localhost:8080");
  }


}
