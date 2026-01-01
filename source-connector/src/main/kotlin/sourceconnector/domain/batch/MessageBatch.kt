package sourceconnector.domain.batch

fun interface MessageBatch<T> {
  fun get(): List<T>
}
