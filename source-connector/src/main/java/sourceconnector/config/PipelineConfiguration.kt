package sourceconnector.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import sourceconnector.domain.log.Log
import sourceconnector.domain.log.factory.JSONLogFactory
import sourceconnector.domain.pipeline.factory.FileBaseLogPipelineBuilder
import sourceconnector.domain.pipeline.factory.FileLogPipelineSupplier
import sourceconnector.domain.pipeline.factory.PipelineBuilder
import sourceconnector.domain.pipeline.factory.PipelineSupplier
import sourceconnector.repository.file.FileRepository
import java.util.function.Supplier

@Configuration
class PipelineConfiguration {
  @Bean
  fun pipelineBuilder(): PipelineBuilder<Log> {
    return FileBaseLogPipelineBuilder()
  }

  @Bean
  fun pipelineSupplier(
    pipelineBuilder: PipelineBuilder<Log>,
    fileRepository: FileRepository,
    pipelineConfig: PipelineConfig
  ): PipelineSupplier<Log> {
    return FileLogPipelineSupplier(
      pipelineBuilder,
      fileRepository,
      JSONLogFactory(),
      Supplier { pipelineConfig.toProcessors() }
    )
  }
}
