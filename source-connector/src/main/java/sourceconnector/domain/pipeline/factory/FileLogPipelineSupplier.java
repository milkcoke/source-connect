package sourceconnector.domain.pipeline.factory;

import lombok.RequiredArgsConstructor;
import sourceconnector.domain.log.Log;
import sourceconnector.domain.log.factory.LogFactory;
import sourceconnector.domain.pipeline.Pipeline;
import sourceconnector.domain.processor.BaseProcessor;
import sourceconnector.repository.file.FileRepository;

import java.util.List;

/**
 * Supplies a {@link Pipeline} instance for processing log files. <br>
 * This has internal cache for providing simple factory API to create {@link Pipeline} instance. <br>
 */
@RequiredArgsConstructor
public class FileLogPipelineSupplier implements PipelineSupplier<Log> {
  private final PipelineBuilder<Log> builder;
  private final FileRepository fileRepository;
  private final LogFactory logFactory;
  private final List<BaseProcessor<Log>> processors;

  @Override
  public Pipeline<Log> get(String filePath) {
    if (processors.isEmpty()) {
      return builder.createWithNoProcessor(fileRepository, filePath, logFactory);
    }
    return builder.create(fileRepository, filePath, logFactory, processors);
  }
}
