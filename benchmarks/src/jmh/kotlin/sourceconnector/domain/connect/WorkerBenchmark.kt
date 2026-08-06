package sourceconnector.domain.connect

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.file.LocalFileKey
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.TopicConfig
import org.apache.kafka.common.errors.TopicExistsException
import org.apache.kafka.common.record.internal.CompressionType
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.LongDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.openjdk.jmh.annotations.*
import org.springframework.kafka.config.TopicBuilder
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
import sourceconnector.repository.offset.InternalOffsetRecordRepository
import sourceconnector.service.offset.OffsetRecordServiceImpl
import java.util.*
import java.util.concurrent.ExecutionException

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 1)
class WorkerBenchmark(
  private val testFileKeys: MutableList<FileKey> = mutableListOf(),
  private val pipelineSupplier: PipelineSupplier<Log> = FileLogPipelineSupplier(
    FileBaseLogPipelineBuilder(),
    LocalFileRepository(),
    JSONLogFactory()
  ) { mutableListOf() },

  private val offsetTopic: NewTopic = TopicBuilder.name("test-offset")
    .compact()
    .partitions(1)
    .replicas(3)
    .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2")
    .config(TopicConfig.SEGMENT_MS_CONFIG, "10000")
    .build(),
  private val logTopic: NewTopic = TopicBuilder.name("test-log")
    .partitions(3)
    .replicas(3)
    .build()
) {

  private lateinit var offsetRecordService: OffsetRecordService
  private lateinit var producerConfig: Properties

  @Setup(Level.Trial)
  fun setup() {
    val testDirectory = java.nio.file.Paths.get("src/jmh/resources/large-testdata")
    val fileKey: FileKey = LocalFileKey.from(testDirectory)

    val fileLister: FileLister = LocalFileLister(
      CompositeFileValidator(
        listOf(
          FileExtensionFilter(listOf(".ndjson"))
        )
      )
    )
    testFileKeys.addAll(fileLister.listFiles(fileKey))

    val adminProps = Properties()
    adminProps[CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9093"

    val adminClient: AdminClient = AdminClient.create(adminProps)
    try {
      adminClient.createTopics(listOf(this.logTopic, this.offsetTopic)).all().get()
    } catch (ignored: TopicExistsException) {
    } catch (e: InterruptedException) {
      throw RuntimeException(e)
    } catch (ignored: ExecutionException) {
    }

    val consumerProps = Properties()
    consumerProps.putAll(
      mapOf(
        CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
        ConsumerConfig.GROUP_ID_CONFIG to "benchmark-offset-consumer",
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to LongDeserializer::class.java
      )
    )

    this.offsetRecordService = OffsetRecordServiceImpl(
      InternalOffsetRecordRepository(
        KafkaConsumer<String, Long>(consumerProps),
        adminClient,
        offsetTopic.name()
      )
    )

    val properties = Properties()
    properties.putAll(
      mapOf(
        CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
        ProducerConfig.ACKS_CONFIG to "-1",
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to ByteArraySerializer::class.java,
        ProducerConfig.COMPRESSION_TYPE_CONFIG to CompressionType.ZSTD.name,
        ProducerConfig.COMPRESSION_ZSTD_LEVEL_CONFIG to 1,
        ProducerConfig.LINGER_MS_CONFIG to 100,
        ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to true
      )
    )
    this.producerConfig = properties
  }

  @Benchmark
  fun singleTaskBenchmark() {
    val worker =
      Worker(0, FileTaskAssignor(this.testFileKeys, 1, this.offsetRecordService))
    worker.createTasks(
      1, 1,
      pipelineSupplier, this.producerConfig,
      logTopic.name(), offsetTopic.name()
    )
    worker.start()
  }


  @Benchmark
  fun fiveTaskBenchmark() {
    val worker =
      Worker(0, FileTaskAssignor(this.testFileKeys, 5, this.offsetRecordService))
    worker.createTasks(
      1, 5,
      pipelineSupplier, this.producerConfig,
      logTopic.name(), offsetTopic.name()
    )
    worker.start()
  }
}
