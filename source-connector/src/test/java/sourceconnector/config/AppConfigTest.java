package sourceconnector.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.boot.convert.ApplicationConversionService;
import sourceconnector.config.util.YamlTestUtils;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

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

  // FIXME: BindException: Failed  to bind properties under 'app.worker-count' to int
  @DisplayName("Yaml configuration test")
  @Test
  void yamlConfigurationTest() throws IOException {
    // given
    Map<String, Object> map = YamlTestUtils.getStringObjectMap("""
      app:
        workerCount: 1
        taskCount: 2
        offsetManagerBaseUrl: http://localhost:8080
      """);
    Binder binder = new Binder(
      List.of(new MapConfigurationPropertySource(map)),
      null,
      ApplicationConversionService.getSharedInstance()
    );

    // when
    assertThatThrownBy(()-> binder.bind("app", AppConfig.class).get())
      .hasRootCauseInstanceOf(NullPointerException.class)
      .hasStackTraceContaining("offsetManagerBaseUrl is required");

  }
}
