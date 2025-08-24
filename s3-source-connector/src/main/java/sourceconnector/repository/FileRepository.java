package sourceconnector.repository;

import java.io.InputStream;

/**
 * Get the file contents from a file repository (e.g. LocalFile System, S3, GCS, Azure Blob Storage, etc.)
 */
public interface FileRepository {
  InputStream getFile(String filePath);
}
