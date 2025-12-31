package sourceconnector.startup

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import sourceconnector.config.ConnectConfig
import sourceconnector.config.TopicConfig
import sourceconnector.domain.connect.FileProcessingResult
import sourceconnector.domain.connect.Task
import sourceconnector.domain.connect.Worker
import sourceconnector.domain.log.Log
import sourceconnector.domain.pipeline.factory.PipelineSupplier
import java.util.*
import kotlin.system.exitProcess

@Component
class WorkerStartupRunner(
  private val connectConfig: ConnectConfig,
  private val worker: Worker,
  private val pipelineSupplier: PipelineSupplier<Log>,
  private val producerProperties: Properties,
  private val topicConfig: TopicConfig,
  private val applicationContext: ApplicationContext,
) : ApplicationRunner {
  private val log = LoggerFactory.getLogger(WorkerStartupRunner::class.java)

  override fun run(args: ApplicationArguments) {
    var exitCode = 0
    try {
      val tasks: Collection<Task<FileProcessingResult>> = worker.createTasks(
        connectConfig.workerCount, connectConfig.taskCount,
        pipelineSupplier,
        producerProperties,
        topicConfig.sinkTopic, topicConfig.offsetTopic
      )
      worker.start()
    } catch (e: Exception) {
      exitCode = 1
      log.error("Error occurred during worker execution", e)
    } finally {
      val finalExitCode = exitCode
      SpringApplication.exit(applicationContext, { finalExitCode })
    }
    log.info("Completed all tasks, shutting down application.")
    exitProcess(exitCode)
  }
}
