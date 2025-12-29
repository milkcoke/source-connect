package sourceconnector.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import sourceconnector.repository.file.validator.FileValidator;

@Import(StorageRepositoryRegistrar.class)
@Configuration
public class StorageRepositoryConfiguration {
  @Bean
  public FileValidator fileValidator(FileSearchConfigs fileSearchConfigs) {
    return fileSearchConfigs.toValidator();
  }

}
