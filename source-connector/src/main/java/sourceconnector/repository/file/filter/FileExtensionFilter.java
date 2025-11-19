package sourceconnector.repository.file.filter;


import offsetmanager.domain.file.FileKey;

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
  public boolean accept(FileKey fileKey) {
    return extensions.stream().anyMatch(extension -> fileKey.get().endsWith(extension));
  }
}
