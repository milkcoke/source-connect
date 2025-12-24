package sourceconnector.repository.file;

import offsetmanager.domain.file.FileKey;

import java.io.IOException;
import java.io.InputStream;

/**
 * Get the file contents from a file storage (e.g. LocalFile System, S3, GCS, Azure Blob Storage, etc.)
 */
public interface FileRepository {
  /**
   *
   * @param fileKey handling file path
   * @return {@link java.io.InputStream}
   * @throws IOException this is unrecoverable so application should be terminated.
   */
  InputStream getFile(FileKey fileKey) throws IOException;
}
