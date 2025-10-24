package sourceconnector.domain.pipeline.factory;

import sourceconnector.domain.log.factory.LogFactory;
import sourceconnector.domain.pipeline.Pipeline;
import sourceconnector.domain.processor.BaseProcessor;
import sourceconnector.repository.file.FileRepository;

import java.util.List;

public interface PipelineBuilder<T> {
  /**
   * Creates a pipeline that consists of one or more processors.
   *
   * @param fileRepository the repository used to retrieve the file from external storage
   * @param filePath the path of the file to be processed
   * @param logFactory the factory used to create {@code Log} instances for pipeline operations
   * @param processors the list of base processors to apply in order
   * @return a constructed {@link sourceconnector.domain.pipeline.Pipeline} instance
   *
   * @throws IllegalStateException if the file cannot be retrieved
   * @throws IllegalArgumentException if {@code processors} is {@code null} or empty
   */
  Pipeline<T> create(
    FileRepository fileRepository,
    String filePath,
    LogFactory logFactory,
    List<BaseProcessor<T>> processors
  );

  /**
   * Creates a pipeline that performs no processing and simply bypasses the input data.
   *
   * @param fileRepository the repository used to retrieve the file from external storage
   * @param filePath the path of the file to be processed
   * @param logFactory the factory used to create {@code Log} instances for pipeline operations
   * @return a constructed {@link sourceconnector.domain.pipeline.Pipeline} instance that bypasses processing
   *
   * @throws IllegalStateException if the file cannot be retrieved
   */
  Pipeline<T> createWithNoProcessor(
    FileRepository fileRepository,
    String filePath,
    LogFactory logFactory
  );
}
