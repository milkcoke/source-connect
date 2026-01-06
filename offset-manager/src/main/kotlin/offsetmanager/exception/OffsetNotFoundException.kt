package offsetmanager.exception
/**
 * Construct offsetmanager.exception avoiding stack trace for performance reasons.
 * @param key object key for which the offset was not found
 */
class OffsetNotFoundException(
  private val key: String
) : RuntimeException(
  "Offset not found for key: $key", null,
  false, false
 )
