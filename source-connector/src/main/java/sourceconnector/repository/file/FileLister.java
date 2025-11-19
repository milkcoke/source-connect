package sourceconnector.repository.file;

import offsetmanager.domain.file.FileKey;

import java.io.IOException;
import java.util.List;

public interface FileLister {
  /**
   * Get file list from a file storage according to the policy
   * @param fileKeys are list of paths to list files from
   * @return list of file paths
   * @throws IOException
   */
  List<FileKey> listFiles(FileKey... fileKeys) throws IOException;

  /**
   * Get file list from a file storage recursively traversing descendant directories
   * @param fileKeys are list of paths to list files from
   * @return list of file paths
   * @throws IOException
   */
  List<FileKey> listFilesRecursively(FileKey... fileKeys) throws IOException;
}
