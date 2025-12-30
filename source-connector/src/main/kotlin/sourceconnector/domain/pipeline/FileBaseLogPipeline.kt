package sourceconnector.domain.pipeline

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.offset.OffsetStatus
import org.slf4j.LoggerFactory
import sourceconnector.domain.log.FileLogMetadata
import sourceconnector.domain.log.Log
import sourceconnector.domain.log.factory.LogFactory
import sourceconnector.domain.processor.BaseProcessor
import sourceconnector.exception.FileLogReadException
import sourceconnector.service.reader.LineReader
import java.io.IOException
import java.lang.AutoCloseable

class FileBaseLogPipeline(
  private val fileKey: FileKey,
  private val reader: LineReader<String>,
  private val logFactory: LogFactory,
  private val startProcessor: BaseProcessor<Log>
) : Pipeline<Log?>, AutoCloseable {
  private val log = LoggerFactory.getLogger(this.javaClass)
  override var isComplete: Boolean = false

  /**
   * @throws FileLogReadException when reading line failed
   * @throws NoSuchElementException when pipeline already completed
   */
  override fun getResult(): Log? {
    // FIXME: Error occurs here when calling getResult after isComplete returns true
    if (this.isComplete) {
      throw NoSuchElementException("No more data")
    }

    try {
      val rawString = this.reader.read() ?: run {
        // Complete the pipeline if end of file has been reached
        this.isComplete = true
        this.close()
        return null
      }

      val input = this.logFactory.create(
        rawString,
        FileLogMetadata(this.fileKey, this.reader.lineNumber.toLong())
      )
      return this.startProcessor.process(input)
    } catch (exception: IOException) {
      throw FileLogReadException(
        String.format(
          "Failed to read from: %s, offset: %d",
          fileKey,
          this.reader.lineNumber + 1
        ),
        exception
      )
    }
  }

  override fun toPosition(offset: Long) {
    require(offset >= OffsetStatus.INITIAL.offset) { "Offset should be greater or equal to zero" }
    try {
      for (i in 0..<offset) {
        this.reader.read() ?: throw FileLogReadException(
          String.format(
            "Offset: %d exceeds file length in file: %s",
            offset,
            fileKey.get()
          ), null
        )
      }
    } catch (e: IOException) {
      throw FileLogReadException(
        String.format("Failed to seek to offset: %d in file: %s", offset, fileKey.get()),
        e.cause
      )
    }
  }

  override fun close() {
    try {
      this.reader.close()
    } catch (e: Exception) {
      log.error("Failed to close reader", e)
    }
  }
}
