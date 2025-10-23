package sourceconnector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import sourceconnector.repository.file.filter.FileExcludeFilter;
import sourceconnector.repository.file.filter.FileExtensionFilter;
import sourceconnector.repository.file.filter.FileFilter;
import sourceconnector.repository.file.filter.FileIncludeFilter;
import sourceconnector.repository.file.validator.CompositeFileValidator;
import sourceconnector.repository.file.validator.FileValidator;
import sourceconnector.repository.file.validator.NoConditionFileValidator;

import java.util.List;

@ConfigurationProperties("app.storage.configs")
public record FiltersConfig(
  List<FilterConfig> filters
) {
  /**
   * Create {@link sourceconnector.repository.file.validator.FileValidator} according `filters` config in order
   */
  public FileValidator toValidator() {
    if (filters == null || filters.isEmpty()) {
      return new NoConditionFileValidator();
    }

    List<FileFilter> fileFilters = filters.stream()
      .map(FilterConfig::toFileFilter)
      .toList();

    return  new CompositeFileValidator(fileFilters);
  }

  record FilterConfig(
    String type,
    List<String> expressions
  ){
    public FileFilter toFileFilter() {
      return switch (type.toLowerCase().trim()) {
        case "exclude"-> new FileExcludeFilter(expressions);
        case "include"-> new FileIncludeFilter(expressions);
        case "extension"-> new FileExtensionFilter(expressions);
        default -> throw new IllegalStateException("Invalid filter type: " + type);
      };

    }
  }

}
