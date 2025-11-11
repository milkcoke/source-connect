package sourceconnector.repository.file;

import sourceconnector.domain.file.FileKey;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.READ;

public class LocalFileRepository implements FileRepository {
  /**
   * Get file from local filesystem
   * @param filePath
   * @return InputStream
   */
  @Override
  public InputStream getFile(FileKey fileKey) throws IOException {
    Path path = Paths.get(fileKey.get());
    return Files.newInputStream(path, READ);
  }
}
