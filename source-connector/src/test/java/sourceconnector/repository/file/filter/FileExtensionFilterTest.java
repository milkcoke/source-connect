package sourceconnector.repository.file.filter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FileExtensionFilterTest {

  @DisplayName("Should accept files with specified extensions")
  @Test
  void accept() {
    // given
    FileFilter filter = new FileExtensionFilter(List.of(".ndjson", ".md"));

    // when
    var result1 = filter.accept("test.ndjson");
    var result2 = filter.accept("README.md");
    var result3 = filter.accept("image.png");
    var result4 = filter.accept("temp.tmp");

    // then
    assertTrue(result1);
    assertTrue(result2);
    assertFalse(result3);
    assertFalse(result4);
  }

  @DisplayName("Should not be empty extensions")
  @Test
  void emptyExtensionTest() {
    Assertions.assertThatThrownBy(() -> new FileExtensionFilter(Collections.emptyList()))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("file extensions cannot be null or empty");
  }
}
