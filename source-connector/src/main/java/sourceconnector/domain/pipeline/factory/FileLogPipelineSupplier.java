package sourceconnector.domain.pipeline.factory;

import lombok.RequiredArgsConstructor;
import sourceconnector.domain.log.Log;
import sourceconnector.domain.log.factory.LogFactory;
import sourceconnector.domain.pipeline.Pipeline;
import sourceconnector.domain.processor.BaseProcessor;
import sourceconnector.repository.file.FileRepository;

import java.util.List;
import java.util.function.Supplier;

/**
 * Supplies a {@link Pipeline} instance for processing log files. <br>
 * This has internal cache for providing simple factory API to create {@link Pipeline} instance. <br>
 */
@RequiredArgsConstructor
public class FileLogPipelineSupplier implements PipelineSupplier<Log> {
  private final PipelineBuilder<Log> builder;
  private final FileRepository fileRepository;
  private final LogFactory logFactory;
  private final Supplier<List<BaseProcessor<Log>>> processorsSupplier;

  @Override
  public Pipeline<Log> get(String filePath) {
    List<BaseProcessor<Log>> processors = processorsSupplier.get();
    if (processors.isEmpty()) {
      return builder.createWithNoProcessor(fileRepository, filePath, logFactory);
    }
    return builder.create(fileRepository, filePath, logFactory, processors);
  }
}
