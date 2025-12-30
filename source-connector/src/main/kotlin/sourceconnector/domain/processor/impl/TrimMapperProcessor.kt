package sourceconnector.domain.processor.impl

import sourceconnector.domain.log.Log
import sourceconnector.domain.log.factory.LogFactory
import sourceconnector.domain.processor.AbstractMapperProcessor

/**
 * Remove the whitespace in the Log
 */
class TrimMapperProcessor(
  private val logFactory: LogFactory
) : AbstractMapperProcessor<Log>() {

  override fun map(input: Log): Log {
    val trimmed = input.get().trim { it <= ' ' }
    return logFactory.create(trimmed, input.metadata)
  }
}
