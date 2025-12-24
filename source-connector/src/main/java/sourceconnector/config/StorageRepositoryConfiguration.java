package sourceconnector.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import sourceconnector.repository.file.*;
import sourceconnector.repository.file.validator.FileValidator;

@Configuration
public class StorageRepositoryConfiguration {

  @Bean
  public FileValidator fileValidator(FileSearchConfigs fileSearchConfigs) {
    return fileSearchConfigs.toValidator();
  }

  // --- S3 beans ---
  @Bean
  @ConditionalOnProperty(prefix = "source.storage", name = "type", havingValue = "s3")
  public S3Client s3Client(S3Config s3Config) {
    return S3Client.builder()
      .region(Region.of(s3Config.region()))
      .build();
  }
  @Bean
  @ConditionalOnProperty(prefix = "source.storage", name = "type", havingValue = "s3")
  public FileLister s3FileLister(S3Client s3Client, FileValidator fileValidator) {
    return new S3FileLister(
      s3Client,
      fileValidator
    );
  }

  @Bean
  @ConditionalOnProperty(prefix = "source.storage", name = "type", havingValue = "s3")
  public FileRepository s3FileRepository(S3Client s3Client) {
    return new S3FileRepository(s3Client);
  }

  // --- Local beans ---
  @Bean
  @ConditionalOnProperty(prefix = "source.storage", name = "type", havingValue = "local")
  public FileLister localFileLister(FileValidator fileValidator) {
    return new LocalFileLister(fileValidator);
  }

  @Bean
  @ConditionalOnProperty(prefix = "source.storage", name = "type", havingValue = "local")
  public FileRepository localFileRepository() {
    return new LocalFileRepository();
  }
}
