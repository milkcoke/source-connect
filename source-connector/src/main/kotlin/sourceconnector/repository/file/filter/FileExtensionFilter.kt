package sourceconnector.repository.file.filter

import offsetmanager.domain.file.FileKey


class FileExtensionFilter(extensions: List<String>) : FileFilter {
  private val extensions: List<String>

  init {
    require(extensions.isNotEmpty()) { "file extensions cannot be null or empty" }
    this.extensions = extensions
  }

  override fun accept(fileKey: FileKey): Boolean {
    return extensions.any { extension: String -> fileKey.get().endsWith(extension) }
  }
}
