package offsetmanager.domain.file;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class S3Uri {
  private final String bucket;
  private final String key;

  public static S3Uri from(String s3Uri) {
    if (s3Uri == null || s3Uri.isEmpty()) {
      throw new IllegalArgumentException("S3 URI cannot be null or empty");
    }
    if (!s3Uri.startsWith("s3://")) {
      throw new IllegalArgumentException("Invalid S3 URI format: " + s3Uri);
    }

    String withoutPrefix = s3Uri.substring(5); // Remove "s3://"
    int slashIndex = withoutPrefix.indexOf('/');
    if (slashIndex == -1) {
      throw new IllegalArgumentException("Invalid S3 URI format: missing key");
    }

    String bucket = withoutPrefix.substring(0, slashIndex);
    String key = withoutPrefix.substring(slashIndex + 1);

    return new S3Uri(bucket, key);
  }

  public static S3Uri of(String bucket, String key) {
    if (bucket == null || bucket.isEmpty()) {
      throw new IllegalArgumentException("Bucket name cannot be null or empty");
    }
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("Key cannot be null or empty");
    }

    return new S3Uri(bucket, key);
  }

  public S3FileKey toFileKey() {
    return new S3FileKey(this);
  }

  public String bucket() {
    return this.bucket;
  }
  public String key() {
    return this.key;
  }

  @Override
  public String toString() {
    return "s3://" + this.bucket + "/" + this.key;
  }
}
