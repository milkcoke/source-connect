package sourceconnector.repository.file.filter;

import java.util.List;

public class FileExtensionFilter implements FileFilter {
  private final List<String> extensions;

  public FileExtensionFilter(List<String> extensions) {
    if (extensions == null || extensions.isEmpty()) {
      throw new IllegalArgumentException("file extensions cannot be null or empty");
    }
    this.extensions = extensions;
  }

  @Override
  public boolean accept(String filePath) {
    return extensions.stream().anyMatch(filePath::endsWith);
  }
}
