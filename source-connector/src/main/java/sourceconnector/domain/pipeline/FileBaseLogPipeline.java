package sourceconnector.domain.pipeline;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sourceconnector.domain.factory.LogFactory;
import sourceconnector.domain.log.Log;
import sourceconnector.domain.log.FileLogMetadata;
import sourceconnector.exception.FileLogReadException;
import sourceconnector.repository.file.FileRepository;
import sourceconnector.domain.processor.BaseProcessor;
import sourceconnector.service.reader.LineReader;
import sourceconnector.service.reader.StringLineReader;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class FileBaseLogPipeline implements Pipeline<Log> {
  private final String filePath;
  private final LineReader<String> reader;
  private final LogFactory logFactory;
  private final BaseProcessor<Log> startProcessor;
  private boolean isComplete = false;

  @SafeVarargs
  public static Pipeline<Log> create(
    FileRepository fileRepository,
    String filePath,
    LogFactory logFactory,
    @NonNull BaseProcessor<Log>... processors
  ) {
    for (int i = processors.length - 1; i > 0; i--) {
      processors[i - 1].setNext(processors[i]);
    }
    try {
      return new FileBaseLogPipeline(
        filePath,
        new StringLineReader(fileRepository.getFile(filePath)),
        logFactory,
        processors[0]
      );
    } catch (IOException e) {
      throw new IllegalArgumentException("Failed to create inputStream from: " + filePath, e);
    }
  }


  @Override
  public Log getResult() {
    try {
      String rawString = this.reader.read();

      // Complete the pipeline if end of file has been reached
      if (rawString == null) {
        this.isComplete = true;
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
}
