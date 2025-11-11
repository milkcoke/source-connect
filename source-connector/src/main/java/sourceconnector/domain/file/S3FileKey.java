package sourceconnector.domain.file;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class S3FileKey implements FileKey {
  private final S3Uri s3Uri;

  public static S3FileKey from(S3Uri s3Uri) {
    return new S3FileKey(s3Uri);
  }

  public static S3FileKey of(String bucket, String objectKey) {
    return new S3FileKey(S3Uri.of(bucket, objectKey));
  }

  @Override
  public String get() {
    return this.s3Uri.toString();
  }
}
