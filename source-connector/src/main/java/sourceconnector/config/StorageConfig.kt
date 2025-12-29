package sourceconnector.config;

import lombok.RequiredArgsConstructor;
import offsetmanager.domain.file.factory.FileKeyParser;
import org.springframework.boot.context.properties.ConfigurationProperties;
import offsetmanager.domain.file.FileKey;

import java.util.List;
import java.util.Objects;

@ConfigurationProperties(prefix = "source.storage")
public record StorageConfig(
  StorageType type,
  List<String> paths
) {
  @RequiredArgsConstructor
  public enum StorageType {
    LOCAL,
    S3;
  }

  public StorageConfig {
    Objects.requireNonNull(type, "storage type is required");
    if (paths == null || paths.isEmpty()) {
      throw new IllegalArgumentException("paths must not be null or empty");
    }
  }

  public List<FileKey> getAllFileKeys() {
    return paths.stream()
      .map(FileKeyParser::parse)
      .toList();
  }
}
