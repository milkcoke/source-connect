package sourceconnector.repository.file.validator

import offsetmanager.domain.file.FileKey

fun interface FileValidator {
  /**
   * Provide should absolute full file object path
   */
  fun isValid(filePath: FileKey): Boolean
}
