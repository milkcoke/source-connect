package sourceconnector.repository.file.options

import offsetmanager.domain.file.LocalFileKey.Companion.from
import org.assertj.core.api.ThrowableAssert
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import sourceconnector.repository.file.filter.FileIncludeFilter
import java.nio.file.Path

internal class FileIncludeFilterTest {
  @DisplayName("Should return true when the file extension is included in the filter")
  @Test
  fun whiteListRegexTest() {
    // given
    val filter = FileIncludeFilter(
      listOf(
        ".*\\.ndjson",
        ".*\\.md$"
      )
    )

    // when
    val result1 = filter.accept(from(Path.of("/Users/Falcon/Downloads/Test.ndjson")))
    val result2 = filter.accept(from(Path.of("README.md")))
    val result3 = filter.accept(from(Path.of("document.txt")))

    // then
    Assertions.assertTrue(result1)
    Assertions.assertTrue(result2)
    Assertions.assertFalse(result3)
  }

  @DisplayName("Should throw IllegalArgumentException when regex list is null or empty")
  @Test
  fun regexExpressionEmptyTest() {
    org.assertj.core.api.Assertions.assertThatThrownBy {
      FileIncludeFilter(
        listOf()
      )
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("regexExpressions cannot be null or empty")
  }
}
