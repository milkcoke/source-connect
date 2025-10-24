package sourceconnector.domain.pipeline;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import sourceconnector.domain.log.factory.LogFactory;
import sourceconnector.domain.log.Log;
import sourceconnector.domain.log.FileLogMetadata;
import sourceconnector.exception.FileLogReadException;
import sourceconnector.domain.processor.BaseProcessor;
import sourceconnector.service.reader.LineReader;

import java.io.IOException;
import java.util.NoSuchElementException;

@Slf4j
@Builder(access = AccessLevel.PUBLIC)
public class FileBaseLogPipeline implements Pipeline<Log>, AutoCloseable {
  private final String filePath;
  private final LineReader<String> reader;
  private final LogFactory logFactory;
  private final BaseProcessor<Log> startProcessor;
  private boolean isComplete = false;

  /**
   * @throws FileLogReadException when reading line failed
   * @throws NoSuchElementException when pipeline already completed
   */
  @Override
  public Log getResult() {
    if (this.isComplete) {
      throw new NoSuchElementException("No more data");
    }

    try {
      String rawString = this.reader.read();

      // Complete the pipeline if end of file has been reached
      if (rawString == null) {
        this.isComplete = true;
        this.close();
        return null;
      }

      Log input = this.logFactory.create(
        rawString,
        new FileLogMetadata(this.filePath, this.reader.getLineNumber())
      );
      return this.startProcessor.process(input);
    } catch (IOException exception) {
      throw new FileLogReadException(
        String.format(
          "Failed to read from: %s, offset: %d",
          filePath,
          this.reader.getLineNumber() + 1
        ),
        exception
      );
    }

  }

  @Override
  public boolean isComplete() {
    return this.isComplete;
  }

  @Override
  public void close() {
    try {
      this.reader.close();
    } catch (Exception e) {
      log.error("Failed to close reader", e);
    }
  }
}
