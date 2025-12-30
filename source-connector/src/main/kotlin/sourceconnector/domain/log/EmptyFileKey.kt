package sourceconnector.domain.log

import offsetmanager.domain.file.FileKey

/**
 * For avoiding null FileKey
 * This is used for {@link EmtpyLogMetadata}
 */
internal class EmptyFileKey : FileKey {
  override fun get(): String = ""
}
