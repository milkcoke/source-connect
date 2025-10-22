package sourceconnector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.storage")
public record StorageConfig(
  StorageType type,
  List<String> paths,
  List<FilterConfig> filters
) {
  public enum StorageType {
    LOCAL,
    S3
  }
  public record FilterConfig(
    String type,
    List<String> expressions
  ) {
  }

}
