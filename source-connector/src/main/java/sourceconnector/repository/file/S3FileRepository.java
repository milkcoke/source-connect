package sourceconnector.repository.file;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import offsetmanager.domain.file.FileKey;

import java.io.InputStream;

@RequiredArgsConstructor
public class S3FileRepository implements FileRepository {
  private final S3Client s3Client;

  @Override
  public InputStream getFile(FileKey fileKey) {
    S3Location s3Location = S3Location.from(fileKey);
    try {
      GetObjectRequest request = GetObjectRequest.builder()
        .bucket(s3Location.bucket())
        .key(s3Location.key())
        .build();

      return s3Client.getObject(request);
    } catch (S3Exception e) {
      throw new RuntimeException("Failed to get file from: " + fileKey.get(), e);
    }
  }
}
