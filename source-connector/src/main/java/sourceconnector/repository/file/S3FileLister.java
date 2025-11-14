package sourceconnector.repository.file;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;
import sourceconnector.domain.file.FileKey;
import sourceconnector.repository.file.validator.FileValidator;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class S3FileLister implements FileLister  {
  private final FileValidator fileValidator;
  private final S3Client s3Client;

  // FIXME: No need bucket name
  public S3FileLister(
    S3Client s3Client,
    FileValidator fileValidator
  ) {
    this.s3Client = s3Client;
    this.fileValidator = fileValidator;
  }

  // TODO: Refactor for using S3 URI creator
  /**
   * Get all s3 object key paths <br>
   * this can handle both directory and file path
   * @return {@code List<String>}
   */
  @Override
  public List<FileKey> listFiles(FileKey... inputFileKeys) {

    List<FileKey> fileKeys = new ArrayList<>();
    for (FileKey fileKey : inputFileKeys) {
      S3Location s3Location = S3Location.from(fileKey);
       ListObjectsV2Request request = ListObjectsV2Request.builder()
        .bucket(s3Location.bucket())
        .prefix(s3Location.key())
        .delimiter("/")
        .build();

      List<FileKey> keys = this.s3Client.listObjectsV2Paginator(request)
        .stream()
        .flatMap(response -> response.contents().stream())
        .map(s3Object -> new S3Location(s3Location.bucket(), s3Object.key()).toFileKey())
        .filter(fileValidator::isValid)
        .toList();

      fileKeys.addAll(keys);
    }

    return fileKeys;
  }

  @Override
  public List<FileKey> listFilesRecursively(FileKey... inputFileKeys) {

    List<FileKey> fileKeys = new ArrayList<>();
    for (FileKey fileKey : inputFileKeys) {
      S3Location s3Location = S3Location.from(fileKey);
      ListObjectsV2Request request = ListObjectsV2Request.builder()
        .bucket(s3Location.bucket())
        .prefix(s3Location.key())
        .build();

      List<FileKey> keys = this.s3Client.listObjectsV2Paginator(request)
        .stream()
        .flatMap(response -> response.contents().stream())
        .map(s3Object -> new S3Location(s3Location.bucket(), s3Object.key()).toFileKey())
        .filter(fileValidator::isValid)
        .toList();

      fileKeys.addAll(keys);
    }

    return fileKeys;
  }
}
