package sourceconnector.domain.batch

class DefaultMessageBatchLogs(
  private val logs: MutableList<String>
) : MessageBatch<String> {

  fun add(log: String) {
    this.logs.add(log)
  }

  fun addAll(logs: MutableList<String>) {
    this.logs.addAll(logs)
  }

  override fun get(): MutableList<String> {
    check(logs.isNotEmpty()) { "No log found" }
    return this.logs
  }
}
