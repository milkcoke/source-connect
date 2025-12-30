package sourceconnector.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import sourceconnector.repository.file.validator.FileValidator

@Import(StorageRepositoryRegistrar::class)
@Configuration
class StorageRepositoryConfiguration {
  @Bean
  fun fileValidator(fileSearchConfigs: FileSearchConfigs): FileValidator {
    return fileSearchConfigs.toValidator()
  }
}
