package offsetmanager.domain.file.factory

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.file.LocalFileKey.Companion.from
import offsetmanager.domain.file.S3Uri
import java.net.URI
import java.nio.file.Path

enum class FileKeyParser(
  private val prefix: String,
  private val parser: (String) -> FileKey
) {
  LOCAL("file:///", { filePath: String -> from(Path.of(URI.create(filePath))) }),
  S3("s3://", { s3Uri: String -> S3Uri.from(s3Uri).toFileKey() });

  companion object {

    private val PARSERS_BY_PREFIX: Map<String, (String) -> FileKey> =
      entries.associate { it.prefix to it.parser }

    @JvmStatic
    fun parse(keyString: String): FileKey =
      PARSERS_BY_PREFIX
        .entries
        .firstOrNull { (prefix, _) -> keyString.startsWith(prefix) }
        ?.value
        ?.invoke(keyString)
        ?: throw IllegalArgumentException("Unsupported file key schema: $keyString")
  }
}
