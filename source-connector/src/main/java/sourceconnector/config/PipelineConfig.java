package sourceconnector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import sourceconnector.domain.factory.JSONLogFactory;
import sourceconnector.domain.log.Log;
import sourceconnector.service.processor.BaseProcessor;
import sourceconnector.service.processor.impl.ByPassProcessor;
import sourceconnector.service.processor.impl.EmptyFilterProcessor;
import sourceconnector.service.processor.impl.TrimMapperProcessor;

import java.util.List;

@ConfigurationProperties("transform")
public record PipelineConfig(
  List<ProcessorConfig> pipeline
) {

  // TODO: is this appropriate deciding creating ByPassProcessor when no processor is provided?
  //  Or make ByPassProcessor list in the FileBaseLogPipeline when no list is provided
  List<BaseProcessor<Log>> toProcessors() {
    if (pipeline == null || pipeline.isEmpty()) {
      return List.of(new ByPassProcessor());
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
