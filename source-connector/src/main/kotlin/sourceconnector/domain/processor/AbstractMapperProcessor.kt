package sourceconnector.domain.processor

abstract class AbstractMapperProcessor<Log> : BaseProcessor<Log>() {
  abstract fun map(input: Log): Log

  override fun process(input: Log): Log? {
    val result = this.map(input)

    if (this.next != null) {
      return this.next!!.process(result)
    }
    return result
  }
}
