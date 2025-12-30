package sourceconnector.domain.processor

abstract class BaseProcessor<T> : Processor<T, T?> {
  protected var next: BaseProcessor<T>? = null

  fun setNext(nextProcessor: BaseProcessor<T>): BaseProcessor<T> {
    this.next = nextProcessor
    return nextProcessor
  }

  /**
   * Process input and return the final output
   */
  abstract override fun process(input: T): T?
}
