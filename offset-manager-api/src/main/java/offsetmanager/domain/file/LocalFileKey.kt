package offsetmanager.domain.file;

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

  @Override
  public String get() {
    return this.fileUri.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof LocalFileKey)) return false;

    return this.get().equals(((LocalFileKey) o).get());
  }

  @Override
  public int hashCode() {
    return this.get().hashCode();
  }
}
