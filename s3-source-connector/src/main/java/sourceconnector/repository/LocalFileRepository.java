package sourceconnector.repository;

import java.io.InputStream;

public class LocalFileRepository implements FileRepository {
  /**
   * Get file from local filesystem
   * @param filePath
   * @return InputStream
   */
  @Override
  public InputStream getFile(String filePath) {
    // TODO: load local file not just only resource file
  }
}
