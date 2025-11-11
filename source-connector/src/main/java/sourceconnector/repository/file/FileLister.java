package sourceconnector.repository.file;

import java.io.IOException;
import java.util.List;

public interface FileLister {
  /**
   * Get file list from a file storage according to the policy
   * @param paths are list of paths to list files from
   * @return list of file paths
   * @throws IOException
   */
  List<String> listFiles(String ...paths) throws IOException;

  /**
   * Get file list from a file storage recursively traversing descendant directories
   * @param paths are list of paths to list files from
   * @return list of file paths
   * @throws IOException
   */
  List<String> listFilesRecursively(String ...paths) throws IOException;
}
