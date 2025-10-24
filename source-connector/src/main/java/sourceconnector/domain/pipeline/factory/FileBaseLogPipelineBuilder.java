package sourceconnector.domain.pipeline.factory;

import sourceconnector.domain.log.Log;
import sourceconnector.domain.log.factory.LogFactory;
import sourceconnector.domain.pipeline.FileBaseLogPipeline;
import sourceconnector.domain.pipeline.Pipeline;
import sourceconnector.domain.processor.BaseProcessor;
import sourceconnector.domain.processor.impl.ByPassProcessor;
import sourceconnector.repository.file.FileRepository;
import sourceconnector.service.reader.StringLineReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class FileBaseLogPipelineBuilder implements PipelineBuilder<Log> {

  @Override
  public Pipeline<Log> create(
    FileRepository fileRepository,
    String filePath,
    LogFactory logFactory,
    List<BaseProcessor<Log>> processors
  ) {
    if (processors == null || processors.isEmpty()) {
      throw new IllegalArgumentException("processors is required");
    }

    // Connect processor in list order
    for (int idx = processors.size() -1 ; idx > 0 ; idx--) {
      processors.get(idx - 1).setNext(processors.get(idx));
    }
    try{
        return FileBaseLogPipeline.builder()
          .filePath(filePath)
          .reader(new StringLineReader(fileRepository.getFile(filePath)))
          .logFactory(logFactory)
          .startProcessor(processors.getFirst())
          .build();
    } catch (IOException e) {
        throw new IllegalStateException("Failed to create log pipeline for file " + filePath, e);
    }

  }

  @Override
  public Pipeline<Log> createWithNoProcessor(
    FileRepository fileRepository,
    String filePath,
    LogFactory logFactory
  ) {
    try {
      InputStream inputStream = fileRepository.getFile(filePath);
      return FileBaseLogPipeline.builder()
        .filePath(filePath)
        .reader(new StringLineReader(inputStream))
        .logFactory(logFactory)
        .startProcessor(new ByPassProcessor())
        .build();
    } catch (IOException e) {
      throw new IllegalStateException("Failed to create pipeline for file " + filePath, e);
    }
  }
}
