package sourceconnector.repository.file.validator

import offsetmanager.domain.file.LocalFileKey.Companion.from
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import sourceconnector.repository.file.filter.FileExtensionFilter
import sourceconnector.repository.file.filter.FileIncludeFilter
import java.nio.file.Path

internal class CompositeFileValidatorTest {
  @DisplayName("Should get only file all conditions are passed")
  @Test
  fun allConditionTest() {
    val validator: FileValidator = CompositeFileValidator(
      listOf(
        FileExtensionFilter(listOf(".ndjson", ".csv")),
        FileIncludeFilter(listOf(".*sample.*"))
      )
    )

    Assertions.assertAll(
      { assertThat(validator.isValid(from(Path.of("sample.ndjson")))).isTrue() },
      { assertThat(validator.isValid(from(Path.of("test.ndjson")))).isFalse() },
      { assertThat(validator.isValid(from(Path.of("sample.csv")))).isTrue() }
    )
  }

  @DisplayName("Should provide one or more condition")
  @Test
  fun provideNoFilterTest() {
    // given
    org.assertj.core.api.Assertions.assertThatThrownBy {
      CompositeFileValidator(emptyList())
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("File filter condition cannot be null or empty")
  }
}
