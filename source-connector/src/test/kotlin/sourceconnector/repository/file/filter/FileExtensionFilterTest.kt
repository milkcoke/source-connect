package sourceconnector.repository.file.filter

import offsetmanager.domain.file.LocalFileKey.Companion.from
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Path

internal class FileExtensionFilterTest {
  @DisplayName("Should accept files with specified extensions")
  @Test
  fun accept() {
    // given
    val filter: FileFilter = FileExtensionFilter(listOf(".ndjson", ".md"))
    // when
    val result1 = filter.accept(from(Path.of("test.ndjson")))
    val result2 = filter.accept(from(Path.of("README.md")))
    val result3 = filter.accept(from(Path.of("image.png")))
    val result4 = filter.accept(from(Path.of("temp.tmp")))

    // then
    Assertions.assertTrue(result1)
    Assertions.assertTrue(result2)
    Assertions.assertFalse(result3)
    Assertions.assertFalse(result4)
  }

  @DisplayName("Should not be empty extensions")
  @Test
  fun emptyExtensionTest() {
    org.assertj.core.api.Assertions.assertThatThrownBy {
      FileExtensionFilter(
        emptyList()
      )
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("file extensions cannot be null or empty")
  }
}
