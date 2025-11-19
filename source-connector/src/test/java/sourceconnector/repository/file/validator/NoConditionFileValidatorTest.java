package sourceconnector.repository.file.validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.file.LocalFileKey;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class NoConditionFileValidatorTest {

  @DisplayName("Should always get true in NoConditionFileValidator")
  @Test
  void validateTest() {
    // given
    FileValidator validator = new NoConditionFileValidator();
    FileKey fileKey1 = LocalFileKey.from(Path.of("sample.ndjson"));
    FileKey fileKey2 = LocalFileKey.from(Path.of("test.ndjson"));
    FileKey fileKey3 = LocalFileKey.from(Path.of("sample.csv"));
    // when then
    assertThat(validator.isValid(fileKey1)).isTrue();
    assertThat(validator.isValid(fileKey2)).isTrue();
    assertThat(validator.isValid(fileKey3)).isTrue();
  }
}
