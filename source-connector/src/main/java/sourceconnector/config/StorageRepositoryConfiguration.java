package sourceconnector.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import sourceconnector.repository.file.*;
import sourceconnector.repository.file.validator.FileValidator;

@Configuration
public class StorageRepositoryConfiguration {

  @Bean
  public FileValidator fileValidator(FiltersConfig filtersConfig) {
    return filtersConfig.toValidator();
  }

  // --- S3 beans ---
  @Bean
  @ConditionalOnProperty(prefix = "app.storage", name = "type", havingValue = "s3")
  public FileLister s3FileLister(S3Config s3Config, FileValidator fileValidator) {
    return new S3FileLister(
      Region.of(s3Config.region()),
      s3Config.bucket(),
      fileValidator
    );
  }

  @Bean
  @ConditionalOnProperty(prefix = "app.storage", name = "type", havingValue = "s3")
  public FileRepository s3FileRepository(S3Config s3Config) {
    return new S3FileRepository(Region.of(s3Config.region()), s3Config.bucket());
  }

  // --- Local beans ---
  @Bean
  @ConditionalOnProperty(prefix = "app.storage", name = "type", havingValue = "local")
  public FileLister localFileLister(FileValidator fileValidator) {
    return new LocalFileLister(fileValidator);
  }

  @Bean
  @ConditionalOnProperty(prefix = "app.storage", name = "type", havingValue = "local")
  public FileRepository localFileRepository() {
    return new LocalFileRepository();
  }
}
