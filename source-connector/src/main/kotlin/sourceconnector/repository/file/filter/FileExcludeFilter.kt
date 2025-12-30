package sourceconnector.repository.file.filter

import offsetmanager.domain.file.FileKey
import java.util.regex.Pattern

class FileExcludeFilter(regexExpressions: List<String>) : FileFilter {
  private val patterns: List<Pattern>

  init {
    require(regexExpressions.isNotEmpty()) { "regexExpressions cannot be null or empty" }
    this.patterns = regexExpressions.stream()
      .map<Pattern> { regex: String -> Pattern.compile(regex) }
      .toList()
  }

  override fun accept(fileKey: FileKey): Boolean {
    return patterns.none { regex -> regex.matcher(fileKey.get()).find() }
  }
}
