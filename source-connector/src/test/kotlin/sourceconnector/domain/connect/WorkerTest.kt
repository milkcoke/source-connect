package sourceconnector.domain.connect

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.file.LocalFileKey.Companion.from
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import sourceconnector.domain.log.Log
import sourceconnector.domain.log.factory.JSONLogFactory
import sourceconnector.domain.pipeline.factory.FileBaseLogPipelineBuilder
import sourceconnector.domain.pipeline.factory.FileLogPipelineSupplier
import sourceconnector.domain.pipeline.factory.PipelineSupplier
import sourceconnector.domain.processor.impl.EmptyFilterProcessor
import sourceconnector.domain.processor.impl.TrimMapperProcessor
import sourceconnector.repository.file.LocalFileRepository
import sourceconnector.repository.offset.InternalOffsetRecordRepository
import sourceconnector.service.offset.OffsetRecordServiceImpl
import sourceconnector.support.KafkaTestSupport
import java.nio.file.Path

internal class WorkerTest : KafkaTestSupport() {
  private val pipelineSupplier: PipelineSupplier<Log> = FileLogPipelineSupplier(
    FileBaseLogPipelineBuilder(),
    LocalFileRepository(),
    JSONLogFactory()
  ) { listOf(EmptyFilterProcessor(), TrimMapperProcessor(JSONLogFactory())) }

  private val offsetTopic = "test-offset-topic"
  private val logTopic = "test-log"

  private lateinit var offsetRecordService: OffsetRecordService

  @BeforeAll
  fun setup() {
    createOffsetTopic(this.offsetTopic, 2)
    createLogTopic(this.logTopic, 3)

    this.offsetRecordService = OffsetRecordServiceImpl(
      InternalOffsetRecordRepository(
        createConsumer(),
        adminClient,
        this.offsetTopic
      )
    )
  }

  @DisplayName("Should throw IllegalArgumentException when worker count is 0")
  @Test
  fun createNoWorkerTest() {
    // given
    val worker = Worker(0, FileTaskAssignor(mutableListOf<FileKey>(), 0, this.offsetRecordService))

    // when then
    Assertions.assertThatThrownBy {
      worker.createTasks(
        0, 1,
        pipelineSupplier, producerProperties,
        this.logTopic, this.offsetTopic
      )
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("Total worker count should be greater than zero")
  }


  @DisplayName("Should throw IllegalArgumentException when task count is 0")
  @Test
  fun createNoTasksTest() {
    // given
    val worker = Worker(0, FileTaskAssignor(mutableListOf<FileKey>(), 0, this.offsetRecordService))

    // when then
    Assertions.assertThatThrownBy {
      worker.createTasks(
        1, 0,
        pipelineSupplier, producerProperties,
        this.logTopic, this.offsetTopic
      )
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("Total task count should be greater than zero")
  }

  @DisplayName("Success to create two tasks in the worker")
  @Test
  fun createTwoTasksTest() {
    // given
    val fileKey1: FileKey = from(Path.of("file-0"))
    val fileKey2: FileKey = from(Path.of("file-1"))

    val worker = Worker(
      0,
      FileTaskAssignor(listOf(fileKey1, fileKey2), 2, this.offsetRecordService)
    )
    // when
    val tasks: Collection<Task<FileProcessingResult>> = worker.createTasks(
      1, 2,
      pipelineSupplier, producerProperties,
      this.logTopic, this.offsetTopic
    )

    // then
    assertThat<Task<FileProcessingResult>>(tasks).hasSize(2)
  }


  @DisplayName("Should throw IllegalStateException when no tasks to start")
  @Test
  fun noTaskStartTest() {
    // given
    val worker = Worker(0, FileTaskAssignor(mutableListOf<FileKey>(), 0, this.offsetRecordService))

    // when then
    Assertions.assertThatThrownBy { worker.start() }
      .isInstanceOf(IllegalStateException::class.java)
      .hasMessage("No tasks to start")
  }

  @DisplayName("Success to start after that tasks are created")
  @Test
  fun successToStart() {
    // given
    val path1 = Path.of("src/test/resources/sample-data/subdir1/sub1.ndjson")
    val path2 = Path.of("src/test/resources/sample-data/empty.ndjson")

    val worker = Worker(
      0,
      FileTaskAssignor(listOf(from(path1), from(path2)),
        2,
        this.offsetRecordService
      )
    )
    val tasks: Collection<Task<FileProcessingResult>> = worker.createTasks(
      1, 2,
      pipelineSupplier, producerProperties,
      this.logTopic, this.offsetTopic
    )

    // when then
    assertDoesNotThrow { worker.start() }
  }
}
