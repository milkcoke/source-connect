package sourceconnector.repository.file.validator;

import sourceconnector.domain.file.FileKey;

@FunctionalInterface
public interface FileValidator {
  /**
   * Provide should absolute full file object path
   */
  boolean isValid(FileKey filePath);
}
