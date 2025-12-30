package sourceconnector.domain.connect

import org.apache.kafka.clients.producer.ProducerConfig
import sourceconnector.domain.log.Log
import sourceconnector.domain.pipeline.factory.PipelineSupplier
import sourceconnector.service.producer.BatchProduceService
import java.util.*
import java.util.concurrent.*
import kotlin.math.min

/**
 * Worker is a container for running tasks.
 */
class Worker(
  private val index: Int,
  private val taskAssignor: TaskAssignor,
  private val id: String = String.format("Worker-%d", index)
) {
  private lateinit var executor: ExecutorService
  private val tasks: MutableCollection<Task<FileProcessingResult>> = mutableListOf()
  private val log = org.slf4j.LoggerFactory.getLogger(Worker::class.java)
  /**
   * Should be called only once after instantiated.
   * @param totalWorkerCount the number of total workers
   * @param totalTaskCount the number of total tasks
   * @return Tasks created in the worker
   */
  fun createTasks(
    totalWorkerCount: Int,
    totalTaskCount: Int,
    pipelineSupplier: PipelineSupplier<Log>,
    producerProperties: Properties,
    logTopic: String,
    offsetTopic: String
  ): Collection<Task<FileProcessingResult>> {
    require(totalWorkerCount >= 1) { "Total worker count should be greater than zero" }
    require(totalTaskCount >= 1) { "Total task count should be greater than zero" }

    val quotient = totalTaskCount / totalWorkerCount
    val remainder = totalTaskCount % totalWorkerCount

    val startIndex = this.index * quotient + min(this.index, remainder)
    val endIndex = (this.index + 1) * quotient + min(this.index + 1, remainder)

    for (taskIndex in startIndex..<endIndex) {
      producerProperties[ProducerConfig.TRANSACTIONAL_ID_CONFIG] = String.format("Task-%d", taskIndex)

      tasks.add(
        FileSourceTask(
          taskIndex,
          pipelineSupplier,
          BatchProduceService(producerProperties, logTopic, offsetTopic)
        )
      )
    }

    this.assignTasks()
    this.executor = Executors.newFixedThreadPool(tasks.size)
    return tasks.toList()
  }


  /**
   * Start all the tasks in this worker
   * FIXME: Should handle IOException when close the producer in the task
   * @throws InterruptedException
   * @throws ExecutionException
   * @throws IllegalStateException trying to start before creating tasks
   */
  @Throws(InterruptedException::class, ExecutionException::class)
  fun start() {
    check(!this.tasks.isEmpty()) { "No tasks to start" }

    try {
      val futures: MutableList<Future<FileProcessingResult>> = this.executor.invokeAll<FileProcessingResult>(tasks)
      for (future in futures) {
        val result = future.get()
        log.info(
          "totalCount: {}, successCount: {}, failCount: {}, skippedCount: {}",
          result.totalCount,
          result.successCount,
          result.failureCount,
          result.skippedCount
        )
      }

      log.info("{} completed the all jobs", this.id)
    } finally {
      this.shutdownGracefully()
    }
  }


  private fun assignTasks() {
    this.taskAssignor.assign(this.tasks)
  }

  private fun shutdownGracefully() {
    this.executor.shutdown()
    try {
      if (!this.executor.awaitTermination(10L, TimeUnit.SECONDS)) {
        log.warn("Failed to termination for 10 Seconds")
        log.warn("Try to shutdown immediately")
        this.executor.shutdownNow()
        if (!this.executor.awaitTermination(5L, TimeUnit.SECONDS)) {
          log.error("All job failed to termination for 5 seconds")
        }
      }
    } catch (e: InterruptedException) {
      this.executor.shutdownNow()
      Thread.currentThread().interrupt()
    }
  }
}
