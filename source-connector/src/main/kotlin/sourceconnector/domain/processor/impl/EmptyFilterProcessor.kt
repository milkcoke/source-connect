package sourceconnector.domain.processor.impl

import sourceconnector.domain.log.Log
import sourceconnector.domain.processor.AbstractFilterProcessor

/**
 * Filter out the empty Log
 */
class EmptyFilterProcessor : AbstractFilterProcessor<Log>() {
  override fun condition(input: Log): Boolean {
    return input.get().isNotBlank()
  }
}
