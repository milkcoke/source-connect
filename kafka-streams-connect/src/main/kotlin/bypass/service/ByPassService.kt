package bypass.service

import bypass.config.TopicConfig
import bypass.domain.s3.S3Path
import bypass.repository.s3.S3Repository
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Produced
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ByPassService(
  private val topicConfig: TopicConfig,
  private val objectMapper: ObjectMapper,
  private val s3Repository: S3Repository,
) {

  @Autowired
  fun buildPipeline(streamsBuilder: StreamsBuilder) {
    val s3Stream : KStream<ByteArray, String> = streamsBuilder.stream(
      topicConfig.sourceTopic,
      Consumed.with(Serdes.ByteArray(), Serdes.String())
    )
    s3Stream
      .mapValues { recordValue ->
        objectMapper.readValue(recordValue, S3Path::class.java)
      }
      .flatMapValues { s3Path ->
       runBlocking {
          s3Repository.findByBucketPath(s3Path)
            .toString(Charsets.UTF_8)
            .trim()
            .split("\n")
            .map { it.toLong() }
        }
      }
      .to(
        topicConfig.sinkTopic,
        Produced.with(Serdes.ByteArray(), Serdes.Long())
      )
  }



}
