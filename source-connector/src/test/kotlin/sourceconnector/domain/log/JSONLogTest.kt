package sourceconnector.domain.log

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.file.LocalFileKey.Companion.from
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Path

internal class JSONLogTest {
  @DisplayName("JSONLog has a payload and fileMetadata")
  @Test
  fun jsonLogTest() {
    // given
    val jsonLog = JSONLog(
      "log payload",
      FileLogMetadata(from(Path.of("file.ndjson")), 0L)
    )
    // when then
    Assertions.assertThat(jsonLog.get()).isEqualTo("log payload")

    val metadata = jsonLog.metadata
    Assertions.assertThat<FileKey>(metadata.key).isEqualTo(from(Path.of("file.ndjson")))
    Assertions.assertThat(metadata.offset).isEqualTo(0L)
  }
}
