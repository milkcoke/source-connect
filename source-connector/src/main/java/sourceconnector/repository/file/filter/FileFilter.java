package sourceconnector.repository.file.filter;

import sourceconnector.domain.file.FileKey;

@FunctionalInterface
public interface FileFilter {
  boolean accept(FileKey fileKey);
}
