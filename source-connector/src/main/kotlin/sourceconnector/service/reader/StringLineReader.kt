package sourceconnector.service.reader

import sourceconnector.exception.FileLogReadException
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.LineNumberReader

class StringLineReader(
  inputStream: InputStream,
  private val reader: LineNumberReader = LineNumberReader(InputStreamReader(inputStream))
) : LineReader<String> {

  @Throws(IOException::class)
  override fun read(): String? {
    return reader.readLine()
  }

  override val lineNumber: Int
    get() = reader.lineNumber

  @Throws(Exception::class)
  override fun close() {
    this.reader.close()
  }

  private fun seekToLine(lineNumber: Int) {
    try {
      for (i in 0..<lineNumber) {
        requireNotNull(this.reader.readLine()) { "Initial line number exceeds last line number" }
      }
    } catch (e: IOException) {
      throw FileLogReadException(e.message!!, e)
    }
  }

  companion object {
    /**
     * Seek the line number updating line number <br></br>
     * Should be called before calling read()
     * @param initialLineNumber position to seek
     * @throws IllegalArgumentException if initial line number be netgative
     * @throws FileLogReadException Failed to readLine() for the input stream.
     */
    @JvmStatic
    fun withInitialLineNumber(
      inputStream: InputStream,
      initialLineNumber: Int
    ): StringLineReader {
      require(initialLineNumber >= 0) { "initial line number must be greater than or equal to 0" }
      val stringLineReader = StringLineReader(inputStream)
      stringLineReader.seekToLine(initialLineNumber)
      return stringLineReader
    }
  }
}
