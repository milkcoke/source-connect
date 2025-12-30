package sourceconnector.domain.log

/**
 * Log Should have the payload and metadata
 */
interface Log {
  val metadata: LogMetadata
  /**
   * Get the payload of the log
   *
   * @return String payload
   */
  fun get(): String
}
