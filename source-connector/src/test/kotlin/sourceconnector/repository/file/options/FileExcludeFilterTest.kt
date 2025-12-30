package sourceconnector.repository.file.options

import offsetmanager.domain.file.LocalFileKey.Companion.from
import org.assertj.core.api.ThrowableAssert
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import sourceconnector.repository.file.filter.FileExcludeFilter
import sourceconnector.repository.file.filter.FileFilter
import java.nio.file.Path

internal class FileExcludeFilterTest {
  @DisplayName("Should exclude files matching the regex patterns")
  @Test
  fun excludeRegexTest() {
    // given
    val filter: FileFilter = FileExcludeFilter(
      listOf(
        ".*\\.tmp$"
      )
    )
    // when
    val result1 = filter.accept(from(Path.of("Test.ndjson")))
    val result2 = filter.accept(from(Path.of("tempfile.tmp")))

    // then
    Assertions.assertTrue(result1)
    Assertions.assertFalse(result2)
  }

  @DisplayName("Should throw IllegalArgumentException when regex list is null or empty")
  @Test
  fun regexExpressionEmptyTest() {
    org.assertj.core.api.Assertions.assertThatThrownBy {
      FileExcludeFilter(emptyList())
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("regexExpressions cannot be null or empty")
  }
}
