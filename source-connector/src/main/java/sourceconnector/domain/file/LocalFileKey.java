package sourceconnector.domain.file;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LocalFileKey implements FileKey {
  private final String path;

  public static LocalFileKey from(Path path) {
    return new LocalFileKey(path.toAbsolutePath().toString());
  }

  @Override
  public String get() {
    return this.path;
  }
}
