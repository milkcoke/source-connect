package sourceconnector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import sourceconnector.domain.log.factory.JSONLogFactory;
import sourceconnector.domain.log.Log;
import sourceconnector.domain.processor.BaseProcessor;
import sourceconnector.domain.processor.impl.EmptyFilterProcessor;
import sourceconnector.domain.processor.impl.TrimMapperProcessor;

import java.util.Collections;
import java.util.List;

@ConfigurationProperties("transform")
public record PipelineConfig(
  List<ProcessorConfig> pipeline
) {

  List<BaseProcessor<Log>> toProcessors() {
    if (pipeline == null || pipeline.isEmpty()) {
      return Collections.emptyList();
    }

    return pipeline.stream()
      .map(ProcessorConfig::toProcessor)
      .toList();
  }

  record ProcessorConfig(
    String type
  ){
    public BaseProcessor<Log> toProcessor() {
      return switch (type.trim().toLowerCase()) {
        case "trim" -> new TrimMapperProcessor(new JSONLogFactory());
        case "skipblank" -> new EmptyFilterProcessor();
        default -> throw new IllegalArgumentException("Invalid processor type: " + type);
      };
    }
  }
}
