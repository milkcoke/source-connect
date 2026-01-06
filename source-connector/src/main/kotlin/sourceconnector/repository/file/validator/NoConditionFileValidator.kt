package sourceconnector.repository.file.validator

import offsetmanager.domain.file.FileKey

class NoConditionFileValidator : FileValidator {
  override fun isValid(filePath: FileKey): Boolean {
    return true
  }
}
