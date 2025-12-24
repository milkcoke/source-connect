package sourceconnector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "connect")
public record ConnectConfig(
  int workerCount,
  int taskCount
) {
  public ConnectConfig {
    if (workerCount < 1) throw new IllegalArgumentException("workerCount must be >= 1");
    if (taskCount < workerCount) throw new IllegalArgumentException("taskCount must be >= workerCount");
  }
}
