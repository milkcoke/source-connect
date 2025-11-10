package sourceconnector.repository.file;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;
import sourceconnector.repository.file.validator.FileValidator;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class S3FileLister implements FileLister  {
  private final FileValidator fileValidator;
  private final S3Client s3Client;
  private final String bucket;

  // FIXME: No need bucket name
  public S3FileLister(
    S3Client s3Client,
    String bucket,
    FileValidator fileValidator
  ) {
    this.s3Client = s3Client;
    this.bucket = bucket;
    this.fileValidator = fileValidator;
  }

  // TODO: Refactor for using S3 URI creator
  /**
   * Get all s3 object key paths <br>
   * this can handle both directory and file path
   * @return {@code List<String>}
   */
  @Override
  public List<String> listFiles(boolean recursive, String... paths) {

    List<String> objectPaths = new ArrayList<>();
    for (String path : paths) {
      ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
        .bucket(this.bucket)
        .prefix(path);

      if (!recursive) {
        requestBuilder.delimiter("/");
      }

      ListObjectsV2Request request = requestBuilder.build();

      List<String> keys = this.s3Client.listObjectsV2Paginator(request)
        .stream()
        .flatMap(response -> response.contents().stream())
        .map(S3Object::key)
        .filter(fileValidator::isValid)
        .map(key -> String.format("s3://%s/%s", this.bucket, key))
        .toList();

      objectPaths.addAll(keys);
    }

    return objectPaths;
  }
}
