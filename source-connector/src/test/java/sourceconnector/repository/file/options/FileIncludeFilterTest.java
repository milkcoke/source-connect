package sourceconnector.repository.file.options;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sourceconnector.repository.file.filter.FileIncludeFilter;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class FileIncludeFilterTest {

  @DisplayName("Should return true when the file extension is included in the filter")
  @Test
  void whiteListRegexTest() {
    // given
    var filter = new FileIncludeFilter(List.of(
      ".*\\.ndjson",
      ".*\\.md$"
    ));

    // when
    var result1 = filter.accept("/Users/Falcon/Downloads/Test.ndjson");
    var result2 = filter.accept("README.md");
    var result3 = filter.accept("document.txt");

    // then
    assertTrue(result1);
    assertTrue(result2);
    assertFalse(result3);
  }

  @DisplayName("Should throw IllegalArgumentException when regex list is null or empty")
  @Test
  void regexExpressionEmptyTest() {
    assertThatThrownBy(()-> new FileIncludeFilter(Collections.emptyList()))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("regexExpressions cannot be null or empty");
  }
}
