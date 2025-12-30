package sourceconnector.repository.file.validator

import offsetmanager.domain.file.FileKey
import sourceconnector.repository.file.filter.FileFilter

class CompositeFileValidator(fileFilters: List<FileFilter>) : FileValidator {
  private val fileFilters: List<FileFilter>

  init {
    require((fileFilters.isNotEmpty())) { "File filter condition cannot be null or empty" }
    this.fileFilters = fileFilters
  }

  /**
   * If no file filter, always return `true`
   * @param filePath to validate
   */
  override fun isValid(filePath: FileKey): Boolean {
    return fileFilters.all { fileFilter -> fileFilter.accept(filePath) }
  }
}
