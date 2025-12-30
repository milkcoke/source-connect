package sourceconnector.repository.file

import offsetmanager.domain.file.FileKey
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class LocalFileRepository : FileRepository {
  /**
   * Get file from local filesystem
   * @param fileKey
   * @return InputStream
   */
  @Throws(IOException::class)
  override fun getFile(fileKey: FileKey): InputStream {
    val path = Paths.get(URI.create(fileKey.get()))
    return Files.newInputStream(path, StandardOpenOption.READ)
  }
}
