package sourceconnector.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import sourceconnector.domain.file.FileKey;
import sourceconnector.domain.file.LocalFileKey;
import sourceconnector.domain.file.S3FileKey;
import sourceconnector.domain.file.S3Uri;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@ConfigurationProperties(prefix = "source.storage")
public record StorageConfig(
  StorageType type,
  List<String> paths
) {
  @RequiredArgsConstructor
  public enum StorageType {
    LOCAL(path -> LocalFileKey.from(Path.of(path))),
    S3(path -> S3FileKey.from(S3Uri.from(path)));

    private final Function<String, FileKey> fileKeyFactory;

    public FileKey toFileKey(String path) {
      return fileKeyFactory.apply(path);
    }
  }
  public StorageConfig {
    Objects.requireNonNull(type, "storage type is required");
    if (paths == null || paths.isEmpty()) {
      throw new IllegalArgumentException("paths must not be null or empty");
    }
  }

  public List<FileKey> getAllFileKeys() {
    return paths.stream()
      .map(type::toFileKey)
      .toList();
  }
}
