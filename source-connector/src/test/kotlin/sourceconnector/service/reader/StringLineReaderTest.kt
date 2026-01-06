package sourceconnector.service.reader

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.file.LocalFileKey.Companion.from
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ThrowableAssert
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import sourceconnector.repository.file.LocalFileRepository
import sourceconnector.service.reader.StringLineReader.Companion.withInitialLineNumber
import java.io.IOException
import java.nio.file.Path

internal class StringLineReaderTest {
  @DisplayName("Should return null when EOF")
  @Test
  @Throws(IOException::class)
  fun readAll() {
    // given
    val localPath = Path.of("src/test/resources/sample-data/large.ndjson")
    val fileKey: FileKey = from(localPath)
    val inputStream = LocalFileRepository().getFile(fileKey)
    val reader: LineReader<String> = StringLineReader(inputStream)

    // when
    var result: String?
    do {
      result = reader.read()
      if (reader.lineNumber.toLong() == 90000L) {
        println(result)
      }
    } while (result != null)

    assertThat(reader.lineNumber).isEqualTo(90000L)
  }

  @DisplayName("Empty line is also counted")
  @Test
  @Throws(IOException::class)
  fun emptyLineTest() {
    // given
    val localPath = Path.of("src/test/resources/sample-data/empty-included.ndjson")
    val fileKey: FileKey = from(localPath)
    val inputStream = LocalFileRepository().getFile(fileKey)
    val reader: LineReader<String> = StringLineReader(inputStream)
    // when
    var result: String? = null
    var count = 0
    while ((reader.read().also { result = it }) != null) {
      println(result)
      count++
    }
    // then
    assertThat(reader.lineNumber).isEqualTo(17L)
    Assertions.assertThat(count).isEqualTo(17)
  }

  @DisplayName("Start at the line number from 0 incremented whenever read()")
  @Test
  @Throws(IOException::class)
  fun startLineNumberTest() {
    // given
    val localPath = Path.of("src/test/resources/sample-data/empty-included.ndjson")
    val fileKey: FileKey = from(localPath)
    val inputStream = LocalFileRepository().getFile(fileKey)
    val reader: LineReader<String> = StringLineReader(inputStream)
    // when then
    assertThat(reader.lineNumber).isEqualTo(0L)
    reader.read()
    assertThat(reader.lineNumber).isEqualTo(1L)
    reader.read()
    assertThat(reader.lineNumber).isEqualTo(2L)
  }

  @DisplayName("Start from the set line number")
  @Test
  @Throws(IOException::class)
  fun startFromSetLineNumberTest() {
    // given
    val localPath = Path.of("src/test/resources/sample-data/line-count.csv")
    val fileKey: FileKey = from(localPath)
    val inputStream = LocalFileRepository().getFile(fileKey)
    // when
    val reader: LineReader<String> = withInitialLineNumber(inputStream, 5)
    // then
    Assertions.assertThat(reader.read()!!.toInt()).isEqualTo(5)
  }

  @DisplayName("Should throw IllegalArgumentException when calling setLineNumber after read() called")
  @Test
  @Throws(
    IOException::class
  )
  fun failSetLineNumberTest() {
    // given
    val localPath = Path.of("src/test/resources/sample-data/line-count.csv")
    val fileKey: FileKey = from(localPath)
    val inputStream = LocalFileRepository().getFile(fileKey)
    // when then
    Assertions.assertThatThrownBy { withInitialLineNumber(inputStream, -100) }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("initial line number must be greater than or equal to 0")
  }
}
