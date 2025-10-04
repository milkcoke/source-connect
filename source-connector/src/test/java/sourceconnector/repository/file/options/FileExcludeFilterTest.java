package sourceconnector.repository.file.options;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sourceconnector.repository.file.filter.FileExcludeFilter;
import sourceconnector.repository.file.filter.FileFilter;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileExcludeFilterTest {

  @DisplayName("Should exclude files matching the regex patterns")
  @Test
  void excludeRegexTest() {
    // given
    FileFilter filter = new FileExcludeFilter(List.of(
      ".*\\.tmp$"
    ));
    // when
    var result1 = filter.accept("/Users/Flacon/Downloads/Test.ndjson");
    var result2 = filter.accept("tempfile.tmp");

    // then
    assertTrue(result1);
    assertFalse(result2);
  }

  @DisplayName("Should throw IllegalArgumentException when regex list is null or empty")
  @Test
  void regexExpressionEmptyTest() {
    Assertions.assertThatThrownBy(()-> new FileExcludeFilter(Collections.emptyList()))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("regexExpressions cannot be null or empty");
  }
}
