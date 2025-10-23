package sourceconnector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Objects;

@ConfigurationProperties(prefix = "app.storage")
public record StorageConfig(
  StorageType type,
  List<String> paths
) {
  public enum StorageType {
    LOCAL,
    S3
  }
  public StorageConfig {
    Objects.requireNonNull(type, "storage type is required");
  }
}
