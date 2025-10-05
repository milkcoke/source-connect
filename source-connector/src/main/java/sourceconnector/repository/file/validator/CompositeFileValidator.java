package sourceconnector.repository.file.validator;

import lombok.RequiredArgsConstructor;
import sourceconnector.repository.file.filter.FileFilter;

import java.util.List;

@RequiredArgsConstructor
public class CompositeFileValidator implements FileValidator {
  private final List<FileFilter> fileFilters;

  /**
   * If no file filter, always return {@code true}
   * @param filePath to validate
   */
  public boolean isValid(String filePath) {
    return this.fileFilters.stream()
      .allMatch(fileFilter -> fileFilter.accept(filePath));
  }
}
