package sourceconnector.repository.file

import offsetmanager.domain.file.FileKey
import java.io.IOException

interface FileLister {
  /**
   * Get file list from a file storage according to the policy
   * @param fileKeys are list of paths to list files from
   * @return list of file paths
   * @throws IOException
   */
  @Throws(IOException::class)
  fun listFiles(vararg fileKeys: FileKey): List<FileKey>

  /**
   * Get file list from a file storage recursively traversing descendant directories
   * @param fileKeys are list of paths to list files from
   * @return list of file paths
   * @throws IOException
   */
  @Throws(IOException::class)
  fun listFilesRecursively(vararg fileKeys: FileKey): List<FileKey>
}
