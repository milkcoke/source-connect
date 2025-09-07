package sourceconnector.domain.log;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JSONLogTest {

  @DisplayName("JSONLog has a payload and fileMetadata")
  @Test
  void jsonLogTest() {
    // given
    JSONLog jsonLog = new JSONLog("log payload",new FileMetadata("localFile/file.ndjson", 0L));
    // when then
    assertThat(jsonLog.get()).isEqualTo("log payload");
    assertThat(jsonLog.getMetadata())
      .extracting(FileMetadata::filePath, FileMetadata::offset)
      .containsExactly("localFile/file.ndjson", 0L);
  }

}
