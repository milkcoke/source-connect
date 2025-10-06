package sourceconnector.repository.file;

import java.io.IOException;
import java.util.List;

public interface FileLister {
  /**
   * TODO: Add file filter e.g. regex, prefix, suffix, etc.
   * Get file list from a file storage according to the policy
   * @param recursive whether to list files recursively
   * @param paths are list of paths to list files from
   * @return list of file paths
   * @throws IOException
   */
  List<String> listFiles(boolean recursive, String ...paths) throws IOException;
}
