package sourceconnector.service.pipeline;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sourceconnector.domain.log.FileMetadata;
import sourceconnector.domain.log.Log;
import sourceconnector.exception.FileLogReadException;
import sourceconnector.parser.LogParser;
import sourceconnector.repository.FileRepository;
import sourceconnector.service.processor.BaseProcessor;
import sourceconnector.service.reader.LineReader;
import sourceconnector.service.reader.StringLineReader;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class FileLogPipeline implements Pipeline<Log> {
  private final String filePath;
  private final LineReader<String> reader;
  private final LogParser parser;
  private final BaseProcessor<Log> startProcessor;
  private boolean isComplete = false;

  @SafeVarargs
  public static Pipeline<Log> create(
    FileRepository fileRepository,
    String filePath,
    LogParser parser,
    @NonNull BaseProcessor<Log>... processors
  ) {
    for (int i = processors.length - 1; i > 0; i--) {
      processors[i - 1].setNext(processors[i]);
    }
    try {
      return new FileLogPipeline(
        filePath,
        new StringLineReader(fileRepository.getFile(filePath)),
        parser,
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

      Log input = this.parser.parse(
        rawString,
        new FileMetadata(this.filePath, this.reader.getLineNumber())
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
