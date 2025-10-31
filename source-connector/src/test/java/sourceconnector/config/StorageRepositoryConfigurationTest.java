package sourceconnector.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import sourceconnector.repository.file.*;
import sourceconnector.repository.file.validator.FileValidator;
import sourceconnector.repository.file.validator.NoConditionFileValidator;

import static org.assertj.core.api.Assertions.assertThat;


class StorageRepositoryConfigurationTest {

  @DisplayName("LocalBean registration test")
  @Test
  void localStorageBeanTest() {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

    TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context, "source.storage.type=local");

    context.register(FiltersConfig.class);
    context.register(StorageRepositoryConfiguration.class);
    context.refresh();

    // when
    FileValidator validator = context.getBean(FileValidator.class);
    FileLister lister = context.getBean(FileLister.class);
    FileRepository repo = context.getBean(FileRepository.class);

    //then
    assertThat(validator).isInstanceOf(NoConditionFileValidator.class);
    assertThat(lister).isInstanceOf(LocalFileLister.class);
    assertThat(repo).isInstanceOf(LocalFileRepository.class);

    context.close();
  }

  @DisplayName("S3 Bean registration test")
  @Test
  void s3StorageBeanTest() {
    // given
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

    TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context, "source.storage.type=s3");

    // Provide S3Config manually
    context.registerBean(S3Config.class, () -> new S3Config("my-bucket", "ap-northeast-2"));
    context.register(FiltersConfig.class);
    context.register(StorageRepositoryConfiguration.class);

    context.refresh();

    // when
    FileValidator validator = context.getBean(FileValidator.class);
    FileLister fileLister = context.getBean(FileLister.class);
    FileRepository repository = context.getBean(FileRepository.class);

    //then
    assertThat(validator).isInstanceOf(NoConditionFileValidator.class);
    assertThat(fileLister).isInstanceOf(S3FileLister.class);
    assertThat(repository).isInstanceOf(S3FileRepository.class);

    context.close();
  }
}
