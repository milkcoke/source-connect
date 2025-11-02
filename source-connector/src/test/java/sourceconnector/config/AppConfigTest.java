package sourceconnector.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.boot.origin.OriginTrackedValue;
import sourceconnector.config.util.YamlTestUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppConfigTest {

  @DisplayName("worker count should be positive")
  @Test
  void workerCountPositiveTest() {
    assertThatThrownBy(()-> new AppConfig(0, 1, URI.create("http://localhost:8080").toURL()))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("workerCount must be >= 1");
  }

  @DisplayName("task count should be greater or equal to the worker count")
  @Test
  void taskCountTest() {
    assertThatThrownBy(()-> new AppConfig(2, 1, URI.create("http://localhost:8080").toURL()))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("taskCount must be >= workerCount");
  }

  @DisplayName("offsetManagerBaseUrl must not be null")
  @Test
  void offsetManagerBaseUrlMissingTest() {
    // given
    assertThatThrownBy(()-> new AppConfig(2, 2, null))
      .isInstanceOf(NullPointerException.class)
      .hasMessage("offsetManagerBaseUrl is required");
  }

  @DisplayName("Yaml configuration test")
  @Test
  void yamlConfigurationTest() throws IOException {
    // given
    Map<String, Object> map = YamlTestUtils.getStringObjectMap("""
      app:
        workerCount: 1
        taskCount: 2
        offsetManagerBaseUrl: 
      """);
    Binder binder = new Binder(new MapConfigurationPropertySource(map));

    // when then
    assertThatThrownBy(()-> binder.bind("app", AppConfig.class).get())
      .hasRootCauseInstanceOf(NullPointerException.class)
      .hasStackTraceContaining("offsetManagerBaseUrl is required");
  }

  @DisplayName("Should throw BindException when invalid Url provided")
  @Test
  void invalidUrlFailTest() throws IOException {
    // given
    Map<String, Object> map = YamlTestUtils.getStringObjectMap("""
      app:
        workerCount: 1
        taskCount: 2
        offsetManagerBaseUrl: localhost:8080
      """);
    Binder binder = new Binder(new MapConfigurationPropertySource(map));
    // when then
    assertThatThrownBy(()-> binder.bind("app", AppConfig.class).get())
      .isInstanceOf(BindException.class);

  }

  @DisplayName("offsetManagerBaseUrl should be parsed as URL")
  @Test
  void urlParseTest() throws IOException {
    // given
    Map<String, Object> map = YamlTestUtils.getStringObjectMap("""
      app:
        workerCount: 1
        taskCount: 2
        offsetManagerBaseUrl: http://localhost:8080
      """);
    Binder binder = new Binder(new MapConfigurationPropertySource(map));
    // when
    AppConfig appConfig = binder.bind("app", AppConfig.class).get();

    // then
    assertThat(appConfig.offsetManagerBaseUrl()).isInstanceOf(URL.class);
  }
}
