package sourceconnector.repository.file.validator

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.file.LocalFileKey.Companion.from
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Path

internal class NoConditionFileValidatorTest {
  @DisplayName("Should always get true in NoConditionFileValidator")
  @Test
  fun validateTest() {
    // given
    val validator: FileValidator = NoConditionFileValidator()
    val fileKey1: FileKey = from(Path.of("sample.ndjson"))
    val fileKey2: FileKey = from(Path.of("test.ndjson"))
    val fileKey3: FileKey = from(Path.of("sample.csv"))
    // when then
    assertThat(validator.isValid(fileKey1)).isTrue()
    assertThat(validator.isValid(fileKey2)).isTrue()
    assertThat(validator.isValid(fileKey3)).isTrue()
  }
}
