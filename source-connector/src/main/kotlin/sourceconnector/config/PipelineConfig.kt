package sourceconnector.config

import org.springframework.boot.context.properties.ConfigurationProperties
import sourceconnector.domain.log.Log
import sourceconnector.domain.log.factory.JSONLogFactory
import sourceconnector.domain.processor.BaseProcessor
import sourceconnector.domain.processor.impl.EmptyFilterProcessor
import sourceconnector.domain.processor.impl.TrimMapperProcessor
import java.util.*

@ConfigurationProperties("processing")
data class PipelineConfig(
  val pipelines: List<ProcessorConfig>?
) {
  fun toProcessors(): List<BaseProcessor<Log>> {
    if (pipelines.isNullOrEmpty()) {
      return listOf()
    }

    return pipelines.stream()
      .map<BaseProcessor<Log>> { obj: ProcessorConfig -> obj.toProcessor() }
      .toList()
  }

  data class ProcessorConfig(
    val type: String
  ) {
    fun toProcessor(): BaseProcessor<Log> {
      return when (type.trim { it <= ' ' }.lowercase(Locale.getDefault())) {
        "trim" -> TrimMapperProcessor(JSONLogFactory())
        "skipblank" -> EmptyFilterProcessor()
        else -> throw IllegalArgumentException("Invalid processor type: $type")
      }
    }
  }
}
