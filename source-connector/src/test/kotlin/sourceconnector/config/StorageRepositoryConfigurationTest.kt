package sourceconnector.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.test.context.support.TestPropertySourceUtils
import sourceconnector.repository.file.*
import sourceconnector.repository.file.validator.FileValidator
import sourceconnector.repository.file.validator.NoConditionFileValidator
import java.util.function.Supplier

internal class StorageRepositoryConfigurationTest {
  @EnableConfigurationProperties(FileSearchConfigs::class)
  internal class TestConfig

  @DisplayName("LocalBean registration test")
  @Test
  fun localStorageBeanTest() {
    val context = org.springframework.context.annotation.AnnotationConfigApplicationContext()

    TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
      context,
      "source.storage.type=local",
      "source.storage.configs.recursive=true"
    )

    context.register(TestConfig::class.java)
    context.register(StorageRepositoryConfiguration::class.java)
    context.refresh()

    // when
    val validator = context.getBean<FileValidator>(FileValidator::class.java)
    val lister = context.getBean<FileLister>(FileLister::class.java)
    val repo = context.getBean<FileRepository>(FileRepository::class.java)

    //then
    assertThat<FileValidator>(validator).isInstanceOf(NoConditionFileValidator::class.java)
    assertThat<FileLister>(lister).isInstanceOf(LocalFileLister::class.java)
    assertThat<FileRepository>(repo).isInstanceOf(DecompressingFileRepository::class.java)

    context.close()
  }

  @DisplayName("S3 Bean registration test")
  @Test
  fun s3StorageBeanTest() {
    // given
    val context = org.springframework.context.annotation.AnnotationConfigApplicationContext()

    TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
      context,
      "source.storage.type=s3",
      "source.storage.configs.recursive=true"
    )

    // Provide S3Config manually
    context.registerBean<S3Config>(S3Config::class.java, Supplier { S3Config("ap-northeast-2") })
    context.register(TestConfig::class.java)
    context.register(StorageRepositoryConfiguration::class.java)

    context.refresh()

    // when
    val validator = context.getBean<FileValidator>(FileValidator::class.java)
    val fileLister = context.getBean<FileLister>(FileLister::class.java)
    val repository = context.getBean<FileRepository>(FileRepository::class.java)

    //then
    assertThat<FileValidator>(validator).isInstanceOf(NoConditionFileValidator::class.java)
    assertThat<FileLister>(fileLister).isInstanceOf(S3FileLister::class.java)
    assertThat<FileRepository>(repository).isInstanceOf(DecompressingFileRepository::class.java)

    context.close()
  }
}
