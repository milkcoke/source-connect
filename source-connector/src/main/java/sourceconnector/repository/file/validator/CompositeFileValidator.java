package sourceconnector.repository.file.validator;

import sourceconnector.repository.file.filter.FileFilter;

import java.util.List;

public class CompositeFileValidator implements FileValidator {
  private final List<FileFilter> fileFilters;

  public CompositeFileValidator(List<FileFilter> fileFilters) {
    if (fileFilters == null || fileFilters.isEmpty()) {
      throw new IllegalArgumentException("File filter condition cannot be null or empty");
    }
    this.fileFilters = fileFilters;
  }
  /**
   * If no file filter, always return {@code true}
   * @param filePath to validate
   */
  public boolean isValid(String filePath) {
    return this.fileFilters.stream()
      .allMatch(fileFilter -> fileFilter.accept(filePath));
  }
}
