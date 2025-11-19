package sourceconnector.repository.file.validator;

import offsetmanager.domain.file.FileKey;

@FunctionalInterface
public interface FileValidator {
  /**
   * Provide should absolute full file object path
   */
  boolean isValid(FileKey filePath);
}
