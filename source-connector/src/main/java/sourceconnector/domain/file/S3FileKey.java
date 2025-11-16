package sourceconnector.domain.file;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class S3FileKey implements FileKey {
  private final S3Uri s3Uri;

  @Override
  public String get() {
    return this.s3Uri.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof S3FileKey)) return false;

    return this.get().equals(((S3FileKey) o).get());
  }

  @Override
  public int hashCode() {
    return this.get().hashCode();
  }
}
