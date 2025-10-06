package sourceconnector.repository.file.validator;

@FunctionalInterface
public interface FileValidator {
  /**
   * Provide should absolute full file object path
   */
  boolean isValid(String filePath);
}
