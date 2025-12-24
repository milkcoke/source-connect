package sourceconnector.domain.log;

import offsetmanager.domain.file.LocalFileKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class JSONLogTest {

  @DisplayName("JSONLog has a payload and fileMetadata")
  @Test
  void jsonLogTest() {
    // given
    JSONLog jsonLog = new JSONLog(
      "log payload",
      new FileLogMetadata(LocalFileKey.from(Path.of("file.ndjson")), 0L)
    );
    // when then
    assertThat(jsonLog.get()).isEqualTo("log payload");

    LogMetadata metadata = jsonLog.getMetadata();
    assertThat(metadata.key()).isEqualTo(LocalFileKey.from(Path.of("file.ndjson")));
    assertThat(metadata.offset()).isEqualTo(0L);
  }

}
