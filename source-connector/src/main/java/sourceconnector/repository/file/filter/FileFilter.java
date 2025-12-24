package sourceconnector.repository.file.filter;

import offsetmanager.domain.file.FileKey;

@FunctionalInterface
public interface FileFilter {
  boolean accept(FileKey fileKey);
}
