package sourceconnector.config

import org.springframework.boot.context.properties.ConfigurationProperties
import sourceconnector.repository.file.filter.FileExcludeFilter
import sourceconnector.repository.file.filter.FileExtensionFilter
import sourceconnector.repository.file.filter.FileFilter
import sourceconnector.repository.file.filter.FileIncludeFilter
import sourceconnector.repository.file.validator.CompositeFileValidator
import sourceconnector.repository.file.validator.FileValidator
import sourceconnector.repository.file.validator.NoConditionFileValidator
import java.util.*

@ConfigurationProperties("source.storage.configs")
class FileSearchConfigs(
  val isRecursive: Boolean = false,
  private val filters: List<FilterConfig>?
) {

  data class FilterConfig(
    val type: String,
    val expressions: List<String>?
  ) {
    fun toFileFilter(): FileFilter {
      return when (type.lowercase(Locale.getDefault()).trim { it <= ' ' }) {
        "exclude" -> FileExcludeFilter(expressions)
        "include" -> FileIncludeFilter(expressions)
        "extension" -> FileExtensionFilter(expressions)
        else -> throw IllegalStateException("Invalid filter type: $type")
      }
    }
  }

  /**
   * Create [FileValidator] according `filters` config in order
   */
  fun toValidator(): FileValidator {
    if (filters.isNullOrEmpty()) {
      return NoConditionFileValidator()
    }

    val fileFilters = filters.stream()
      .map<FileFilter?> { obj: FilterConfig? -> obj!!.toFileFilter() }
      .toList()

    return CompositeFileValidator(fileFilters)
  }
}

