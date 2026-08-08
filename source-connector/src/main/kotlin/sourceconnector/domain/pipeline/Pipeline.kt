package sourceconnector.domain.pipeline

/**
 * Pipeline consists of multiple processors and can get result
 * @param <T> Handling type
</T> */
interface Pipeline<T> : AutoCloseable {
  var isComplete: Boolean

  fun getResult(): T?
  /**
   * Move the pipeline to the given offset position
   * @param offset
   * @throws IllegalArgumentException when the given offset is negative
   */
  fun toPosition(offset: Long)
}
