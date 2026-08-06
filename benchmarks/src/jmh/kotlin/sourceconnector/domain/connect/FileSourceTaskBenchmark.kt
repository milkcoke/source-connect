package sourceconnector.domain.connect

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.file.LocalFileKey.Companion.from
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.record.internal.CompressionType
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringSerializer
import org.openjdk.jmh.annotations.*
import sourceconnector.domain.log.Log
import sourceconnector.domain.log.factory.JSONLogFactory
import sourceconnector.domain.pipeline.factory.FileBaseLogPipelineBuilder
import sourceconnector.domain.pipeline.factory.FileLogPipelineSupplier
import sourceconnector.domain.pipeline.factory.PipelineSupplier
import sourceconnector.repository.file.FileLister
import sourceconnector.repository.file.LocalFileLister
import sourceconnector.repository.file.LocalFileRepository
import sourceconnector.repository.file.filter.FileExtensionFilter
import sourceconnector.repository.file.validator.CompositeFileValidator
import sourceconnector.service.producer.BatchProduceService
import java.nio.file.Paths
import java.util.*

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 1)
class FileSourceTaskBenchmark(
  private val properties: Properties = Properties(),
  private val fileLister: FileLister = LocalFileLister(
    CompositeFileValidator(
      listOf(
        FileExtensionFilter(listOf(".ndjson"))
      )
    )
  ),
  private val pipelineSupplier: PipelineSupplier<Log> = FileLogPipelineSupplier(
    FileBaseLogPipelineBuilder(),
    LocalFileRepository(),
    JSONLogFactory()
  ) { listOf() },
  private val testFilePaths: MutableList<FileKey> = mutableListOf()
) {

  @Setup(Level.Trial)
  fun setup() {
    this.properties.putAll(
      mapOf(
        CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
        ProducerConfig.ACKS_CONFIG to "-1",
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to ByteArraySerializer::class.java,
        ProducerConfig.COMPRESSION_TYPE_CONFIG to CompressionType.ZSTD.name,
        ProducerConfig.COMPRESSION_ZSTD_LEVEL_CONFIG to 1,
        ProducerConfig.LINGER_MS_CONFIG to 100,
        ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to true,
        ProducerConfig.TRANSACTIONAL_ID_CONFIG to "single-task"
      )
    )

    val testDirectory = Paths.get("src/jmh/resources/testdata")
    val fileKey: FileKey = from(testDirectory)
    testFilePaths.addAll(fileLister.listFiles(fileKey))
  }

  @Benchmark
  fun singleTaskBenchmark(): FileProcessingResult {
    val task: Task<FileProcessingResult> = FileSourceTask(
      0,
      pipelineSupplier,
      BatchProduceService(properties, "log-topic", "offset-topic")
    )
    val offsetMap : Map<FileKey, Long> = this.testFilePaths.associateWith{ 0L }
    task.assign(offsetMap)
    return task.call()
  }
}
