package sourceconnector.domain.processor

abstract class AbstractFilterProcessor<Log> : BaseProcessor<Log>() {
  protected abstract fun condition(input: Log): Boolean

  override fun process(input: Log): Log? {
    if (!this.condition(input)) return null
    if (this.next == null) return input
    return this.next!!.process(input)
  }
}
