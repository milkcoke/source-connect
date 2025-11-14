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
}
