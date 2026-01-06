package sourceconnector.domain.processor

interface Processor<I, R> {
  fun process(record: I): R
}
