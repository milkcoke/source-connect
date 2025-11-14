package sourceconnector.repository.file;

import sourceconnector.domain.file.FileKey;
import sourceconnector.domain.file.S3FileKey;
import sourceconnector.domain.file.S3Uri;

public record S3Location(
  String bucket,
  String key
) {
  public S3Location {
    if (bucket == null || bucket.isBlank()) {
      throw new IllegalArgumentException("Bucket must not be null or blank");
    }
    if (key == null || key.isBlank()) {
      throw new IllegalArgumentException("S3 Key must not be null or blank");
    }
  }

  public static S3Location from(S3Uri s3Uri) {
    return new S3Location(s3Uri.bucket(), s3Uri.key());
  }

  public static S3Location from(FileKey fileKey) {
    S3Uri s3Uri = S3Uri.from(fileKey.get());
    return new S3Location(s3Uri.bucket(), s3Uri.key());
  }

  public FileKey toFileKey() {
    return S3Uri.of(bucket, key).toFileKey();
  }
}
