package sourceconnector.domain.connect

import offsetmanager.domain.file.FileKey
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import sourceconnector.domain.log.Log
import sourceconnector.domain.log.factory.JSONLogFactory
import sourceconnector.domain.pipeline.factory.FileBaseLogPipelineBuilder
import sourceconnector.domain.pipeline.factory.FileLogPipelineSupplier
import sourceconnector.domain.pipeline.factory.PipelineSupplier
import sourceconnector.repository.file.LocalFileRepository
import sourceconnector.repository.offset.InternalOffsetRecordRepository
import sourceconnector.service.offset.OffsetRecordServiceImpl
import sourceconnector.service.producer.BatchProduceService
import sourceconnector.support.KafkaTestSupport

internal class FileTaskAssignorTest : KafkaTestSupport() {
  private val pipelineSupplier: PipelineSupplier<Log> = FileLogPipelineSupplier(
    FileBaseLogPipelineBuilder(),
    LocalFileRepository(),
    JSONLogFactory()
  ) { mutableListOf() }
  private lateinit var offsetRecordService: OffsetRecordService

  private val offsetTopic = "test-offset"
  private val logTopic = "test-log"

  @BeforeAll
  fun setUp() {
    createOffsetTopic(this.offsetTopic, 3)
    createLogTopic(this.logTopic, 3)
    this.offsetRecordService = OffsetRecordServiceImpl(
      InternalOffsetRecordRepository(
        createConsumer(),
        adminClient,
        this.offsetTopic
      )
    )
  }

  internal class FakeFileKey(
    private val path: String? = null
  ) : FileKey {
    override fun get(): String {
      return ""
    }
  }

  @DisplayName("First task has the number of 6 file paths when task 5 and file paths 30 provided")
  @Test
  fun assignedFileCountTest() {
    // given

    val fileKeys: MutableList<FileKey> = mutableListOf()
    for (i in 0..29) {
      fileKeys.add(FakeFileKey("file-$i"))
    }
    val taskAssignor: TaskAssignor = FileTaskAssignor(fileKeys, 5, this.offsetRecordService)
    val task0 = FileSourceTask(
      0, pipelineSupplier,
      BatchProduceService(producerProperties, this.offsetTopic, this.logTopic)
    )

    // when
    taskAssignor.assign(listOf(task0))

    // then
    assertThat(task0.fileKeyOffsetMap)
      .hasSize(6)
  }

  @DisplayName("First and second task have the number of 8 file paths when task 4 and file paths 30 provided")
  @Test
  fun assignedFileCountTest2() {
    // given
    val fileKeys: MutableList<FileKey> = mutableListOf()
    for (i in 0..29) {
      fileKeys.add(FakeFileKey("file-$i"))
    }
    val taskAssignor: TaskAssignor = FileTaskAssignor(fileKeys.toList(), 4, this.offsetRecordService)
    val task0 =
      FileSourceTask(0, pipelineSupplier, BatchProduceService(producerProperties, this.logTopic, this.offsetTopic))
    val task1 =
      FileSourceTask(1, pipelineSupplier, BatchProduceService(producerProperties, this.logTopic, this.offsetTopic))
    val task2 =
      FileSourceTask(2, pipelineSupplier, BatchProduceService(producerProperties, this.logTopic, this.offsetTopic))
    val task3 =
      FileSourceTask(3, pipelineSupplier, BatchProduceService(producerProperties, this.logTopic, this.offsetTopic))

    val tasks: Collection<FileSourceTask> = listOf(task0, task1, task2, task3)

    // when
    taskAssignor.assign(tasks)

    // then
    Assertions.assertAll(
      { assertThat(task0.fileKeyOffsetMap).hasSize(8) },
      { assertThat(task1.fileKeyOffsetMap).hasSize(8) },
      { assertThat(task2.fileKeyOffsetMap).hasSize(7) },
      { assertThat(task3.fileKeyOffsetMap).hasSize(7) }
    )
  }

  @DisplayName("Can assign even though file path has no element")
  @Test
  fun emptyFilePathAssignTest() {
    // given
    val taskAssignor: TaskAssignor = FileTaskAssignor(mutableListOf<FileKey>(), 1, this.offsetRecordService)
    val tasks: Collection<Task<FileProcessingResult>> = listOf<Task<FileProcessingResult>>(
      FileSourceTask(0, pipelineSupplier, BatchProduceService(producerProperties, this.logTopic, this.offsetTopic))
    )

    // when then
    Assertions.assertDoesNotThrow { taskAssignor.assign(tasks) }
  }
}
