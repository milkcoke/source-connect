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
    if (workerCount < 1) throw new IllegalArgumentException("workerCount >= 1 required");
    if (taskCount < workerCount) throw new IllegalArgumentException("taskCount >= workerCount required");
    Objects.requireNonNull(offsetManagerBaseUrl, "offsetManagerBaseUrl is required");
  }
}
