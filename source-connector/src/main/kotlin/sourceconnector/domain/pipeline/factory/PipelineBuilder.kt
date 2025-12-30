package sourceconnector.domain.pipeline.factory

import offsetmanager.domain.file.FileKey
import sourceconnector.domain.log.factory.LogFactory
import sourceconnector.domain.pipeline.Pipeline
import sourceconnector.domain.processor.BaseProcessor
import sourceconnector.repository.file.FileRepository

interface PipelineBuilder<T> {
  /**
   * Creates a pipeline that consists of one or more processors.
   *
   * @param fileRepository the repository used to retrieve the file from external storage
   * @param fileKey the path of the file to be processed
   * @param logFactory the factory used to create `Log` instances for pipeline operations
   * @param processors the list of base processors to apply in order
   * @return a constructed [Pipeline] instance
   *
   * @throws IllegalStateException if the file cannot be retrieved
   * @throws IllegalArgumentException if `processors` is `null` or empty
   */
  fun create(
    fileRepository: FileRepository,
    fileKey: FileKey,
    logFactory: LogFactory,
    processors: List<BaseProcessor<T>>
  ): Pipeline<T?>

  /**
   * Creates a pipeline that performs no processing and simply bypasses the input data.
   *
   * @param fileRepository the repository used to retrieve the file from external storage
   * @param fileKey the path of the file to be processed
   * @param logFactory the factory used to create `Log` instances for pipeline operations
   * @return a constructed [Pipeline] instance that bypasses processing
   *
   * @throws IllegalStateException if the file cannot be retrieved
   */
  fun createWithNoProcessor(
    fileRepository: FileRepository,
    fileKey: FileKey,
    logFactory: LogFactory
  ): Pipeline<T?>
}
