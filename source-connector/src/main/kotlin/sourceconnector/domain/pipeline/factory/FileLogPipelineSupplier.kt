package sourceconnector.domain.pipeline.factory

import offsetmanager.domain.file.FileKey
import sourceconnector.domain.log.Log
import sourceconnector.domain.log.factory.LogFactory
import sourceconnector.domain.pipeline.Pipeline
import sourceconnector.domain.processor.BaseProcessor
import sourceconnector.repository.file.FileRepository
import java.util.function.Supplier

/**
 * Supplies a [Pipeline] instance for processing log files. <br></br>
 * This has internal cache for providing simple factory API to create [Pipeline] instance. <br></br>
 */
class FileLogPipelineSupplier(
  private val builder: PipelineBuilder<Log>,
  private val fileRepository: FileRepository,
  private val logFactory: LogFactory,
  private val processorsSupplier: Supplier<List<BaseProcessor<Log>>>
) : PipelineSupplier<Log> {

  override fun get(fileKey: FileKey): Pipeline<Log?> {
    val processors = processorsSupplier.get()
    if (processors.isEmpty()) {
      return builder.createWithNoProcessor(fileRepository, fileKey, logFactory)
    }
    return builder.create(fileRepository, fileKey, logFactory, processors)
  }
}
