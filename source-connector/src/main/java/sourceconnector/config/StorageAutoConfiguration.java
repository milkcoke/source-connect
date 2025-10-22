package sourceconnector.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import sourceconnector.repository.file.*;

@Configuration
public class StorageAutoConfiguration {
  // --- S3 beans ---
  /*@Bean
  @ConditionalOnProperty(prefix = "app.storage", name = "type", havingValue = "s3", matchIfMissing = false)
  public S3Config s3Config(S3Config s3Config) {
    var s3Config = storageConfig.s3Config();
    return new StorageConfig.S3Config(s3Config.region(), s3Config.bucket());
  }

  @Bean
  @ConditionalOnProperty(prefix = "app.storage", name = "type", havingValue = "s3")
  public FileLister s3FileLister(StorageConfig storageConfig) {
    var  s3Config = storageConfig.s3Config();
    return new S3FileLister(s3Config.region(), s3Config.bucket())
  }

  @Bean
  @ConditionalOnProperty(prefix = "app.storage", name = "type", havingValue = "s3")
  public FileRepository s3FileRepository(S3Config s3Config) {
    return new S3FileRepository(Region.of(s3Config.region()), s3Config.bucket());
  }

  // --- Local beans ---
  @Bean
  @ConditionalOnProperty(prefix = "app.storage", name = "type", havingValue = "local")
  public StorageConfig.LocalConfig localConfig(StorageProperties props) {
    return new StorageConfig.LocalConfig(props.local().baseDir());
  }

  @Bean
  @ConditionalOnProperty(prefix = "app.storage", name = "type", havingValue = "local")
  public FileLister localFileLister(StorageConfig.LocalConfig localConfig) {
    return new LocalFileLister(localConfig.baseDir());
  }

  @Bean
  @ConditionalOnProperty(prefix = "app.storage", name = "type", havingValue = "local")
  public FileRepository localFileRepository(StorageConfig.LocalConfig localConfig) {
    return new LocalFileRepository(localConfig.baseDir());
  }
   */
}
