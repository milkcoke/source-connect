package sourceconnector.domain.file;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.nio.file.Path;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LocalFileKey implements FileKey {
  private final URI fileUri;

  public static LocalFileKey from(Path path) {
    return new LocalFileKey(path.toUri());
  }

  // TODO: Implement Comparable, Equals, HasCode
  @Override
  public String get() {
    return this.fileUri.toString();
  }
}
