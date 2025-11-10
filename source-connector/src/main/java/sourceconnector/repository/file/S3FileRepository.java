package sourceconnector.repository.file;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.InputStream;

public class S3FileRepository implements FileRepository {
  private final S3Client s3Client;
  private final String bucket;

  // FIXME: NO need bucket name
  public S3FileRepository(S3Client s3Client, String bucket) {
    this.s3Client = s3Client;
    this.bucket = bucket;
  }

  @Override
  public InputStream getFile(String filePath) {
    try {
      GetObjectRequest request = GetObjectRequest.builder()
        .bucket(this.bucket)
        .key(filePath)
        .build();

      return s3Client.getObject(request);
    } catch (S3Exception e) {
      throw new RuntimeException("Failed to get file from S3: " + filePath, e);
    }
  }
}
