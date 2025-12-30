package sourceconnector.domain.processor.impl

import sourceconnector.domain.log.Log
import sourceconnector.domain.processor.BaseProcessor

/**
 * Bypass the input with no operation
 */
class ByPassProcessor : BaseProcessor<Log>() {
  override fun process(input: Log): Log? {
    if (this.next == null) return input
    return this.next!!.process(input)
  }
}
