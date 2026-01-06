package sourceconnector.domain.log

class JSONLog(
  private val payload: String,
  override val metadata: LogMetadata
) : Log {

  fun withPayload(newPayload: String): JSONLog {
    return JSONLog(newPayload, this.metadata)
  }

  override fun get(): String {
    return this.payload
  }
}
