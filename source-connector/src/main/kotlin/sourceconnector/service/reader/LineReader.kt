package sourceconnector.service.reader

interface LineReader<T> : Reader<T> {
  val lineNumber: Int
}
