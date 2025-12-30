package sourceconnector.repository.file

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.file.LocalFileKey
import offsetmanager.domain.file.LocalFileKey.Companion.from
import sourceconnector.repository.file.validator.FileValidator
import java.io.IOException
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

class LocalFileLister(
  private val fileValidator: FileValidator
) : FileLister {

  override fun listFiles(vararg fileKeys: FileKey): List<FileKey> {
    require(fileKeys.isNotEmpty()) { "paths cannot be empty" }

    val result: MutableList<FileKey> = mutableListOf()

    for (fileKey in fileKeys) {
      val absolutePath = Path.of(URI.create(fileKey.get())).toAbsolutePath()
      this.validatePathExists(absolutePath)
      if (Files.isRegularFile(absolutePath)) {
        result.addAll(this.handleFile(absolutePath))
      } else if (Files.isDirectory(absolutePath)) {
        result.addAll(this.handleDirectory(absolutePath))
      }
    }
    return result.toList()
  }

  override fun listFilesRecursively(vararg fileKeys: FileKey): List<FileKey> {
    require(fileKeys.isNotEmpty()) { "paths cannot be null or empty" }

    val result: MutableList<FileKey> = mutableListOf()
    for (fileKey in fileKeys) {
      val absolutePath = Path.of(URI.create(fileKey.get())).toAbsolutePath()
      this.validatePathExists(absolutePath)
      if (Files.isRegularFile(absolutePath)) {
        result.addAll(this.handleFile(absolutePath))
      } else if (Files.isDirectory(absolutePath)) {
        result.addAll(this.handleDirectoryRecursively(absolutePath))
      }
    }

    return result
  }

  private fun validatePathExists(absPath: Path) {
    require(Files.exists(absPath)) { "path does not exist: $absPath" }
  }

  private fun handleFile(absFilePath: Path): List<FileKey> {
    return if (fileValidator.isValid(from(absFilePath)))
      listOf<FileKey>(from(absFilePath))
    else listOf()
  }


  private fun handleDirectory(absDir: Path): List<FileKey> {
    try {
      Files.list(absDir).use { stream ->
        return stream
          .filter { path: Path -> Files.isRegularFile(path) }
          .map<LocalFileKey> { obj: Path -> LocalFileKey.from(obj) }
          .filter { filePath: LocalFileKey -> fileValidator.isValid(filePath) }
          .collect(Collectors.toUnmodifiableList())
      }
    } catch (_: IOException) {
      throw IllegalArgumentException("failed to list files in directory: $absDir")
    }
  }

  private fun handleDirectoryRecursively(absDir: Path): List<FileKey> {
    try {
      Files.walk(absDir, Int.MAX_VALUE).use { stream ->
        return stream
          .filter { path: Path -> Files.isRegularFile(path) }
          .map<LocalFileKey> { obj: Path -> LocalFileKey.from(obj) }
          .filter { filePath: LocalFileKey -> fileValidator.isValid(filePath) }
          .collect(Collectors.toUnmodifiableList())
      }
    } catch (_: IOException) {
      throw IllegalArgumentException("failed to list files in directory: $absDir")
    }
  }
}
