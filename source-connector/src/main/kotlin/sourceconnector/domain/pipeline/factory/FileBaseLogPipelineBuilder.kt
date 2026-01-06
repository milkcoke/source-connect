package sourceconnector.domain.pipeline.factory

import offsetmanager.domain.file.FileKey
import sourceconnector.domain.log.Log
import sourceconnector.domain.log.factory.LogFactory
import sourceconnector.domain.pipeline.FileBaseLogPipeline
import sourceconnector.domain.pipeline.Pipeline
import sourceconnector.domain.processor.BaseProcessor
import sourceconnector.domain.processor.impl.ByPassProcessor
import sourceconnector.repository.file.FileRepository
import sourceconnector.service.reader.StringLineReader
import java.io.IOException

class FileBaseLogPipelineBuilder : PipelineBuilder<Log> {
  override fun create(
    fileRepository: FileRepository,
    fileKey: FileKey,
    logFactory: LogFactory,
    processors: List<BaseProcessor<Log>>
  ): Pipeline<Log?> {
    require(processors.isNotEmpty()) { "processors is required" }

    // Connect processor in list order
    for (idx in processors.size - 1 downTo 1) {
      processors[idx - 1].setNext(processors[idx])
    }
    try {
      return FileBaseLogPipeline(
        fileKey = fileKey,
        reader = StringLineReader(fileRepository.getFile(fileKey)),
        logFactory = logFactory,
        startProcessor = processors.first()
      )
    } catch (e: IOException) {
      throw IllegalStateException("Failed to create log pipeline for file " + fileKey.get(), e)
    }
  }

  override fun createWithNoProcessor(
    fileRepository: FileRepository,
    fileKey: FileKey,
    logFactory: LogFactory
  ): Pipeline<Log?> {
    try {
      val inputStream = fileRepository.getFile(fileKey)
      return FileBaseLogPipeline(
        fileKey = fileKey,
        reader = StringLineReader(inputStream),
        logFactory = logFactory,
        startProcessor = ByPassProcessor()
        )
    } catch (e: IOException) {
      throw IllegalStateException("Failed to create pipeline for file " + fileKey.get(), e)
    }
  }
}
