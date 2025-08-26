package sourceconnector.repository;

import java.io.IOException;
import java.io.InputStream;

/**
 * Get the file contents from a file repository (e.g. LocalFile System, S3, GCS, Azure Blob Storage, etc.)
 */
public interface FileRepository {
  /**
   *
   * @param filePath handling file path
   * @return {@link java.io.InputStream}
   * @throws IOException this is unrecoverable so application should be terminated.
   */
  InputStream getFile(String filePath) throws IOException;
}
