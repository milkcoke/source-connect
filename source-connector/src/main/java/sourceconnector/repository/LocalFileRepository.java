package sourceconnector.repository;

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
  public InputStream getFile(String filePath) throws IOException {
    Path path = Paths.get(filePath);
    return Files.newInputStream(path, READ);
  }
}
