package sourceconnector.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sourceconnector.domain.log.Log;
import sourceconnector.domain.log.factory.JSONLogFactory;
import sourceconnector.domain.pipeline.factory.FileBaseLogPipelineBuilder;
import sourceconnector.domain.pipeline.factory.FileLogPipelineSupplier;
import sourceconnector.domain.pipeline.factory.PipelineBuilder;
import sourceconnector.domain.pipeline.factory.PipelineSupplier;
import sourceconnector.repository.file.FileRepository;

@Configuration
public class PipelineConfiguration {
  @Bean
  PipelineBuilder<Log> pipelineBuilder() {
    return new FileBaseLogPipelineBuilder();
  }

  @Bean
  PipelineSupplier<Log> pipelineSupplier(
    PipelineBuilder<Log> pipelineBuilder,
    FileRepository fileRepository,
    PipelineConfig pipelineConfig
    ) {
    return new FileLogPipelineSupplier(
        pipelineBuilder,
        fileRepository,
        new JSONLogFactory(),
        pipelineConfig::toProcessors
      );
  }
}
