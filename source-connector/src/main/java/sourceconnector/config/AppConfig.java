package sourceconnector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URL;
import java.util.Objects;

@ConfigurationProperties(prefix = "app")
public record AppConfig(
  int workerCount,
  int taskCount,
  URL offsetManagerBaseUrl
) {
  public AppConfig {
    if (workerCount < 1) throw new IllegalArgumentException("workerCount must be >= 1");
    if (taskCount < workerCount) throw new IllegalArgumentException("taskCount must be >= workerCount");
    Objects.requireNonNull(offsetManagerBaseUrl, "offsetManagerBaseUrl is required");
  }
}
