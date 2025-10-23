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

@ConfigurationProperties("app.storage.filters")
public record FiltersConfig(
  List<FilterConfig> filtersConfigs
) {
  public FileValidator toValidator() {
    if (filtersConfigs == null || filtersConfigs.isEmpty()) {
      return new NoConditionFileValidator();
    }

    List<FileFilter> filters = filtersConfigs.stream()
      .map(FilterConfig::toFileFilter)
      .toList();

    return  new CompositeFileValidator(filters);
  }

  record FilterConfig(
    String type,
    List<String> expressions
  ){
    public FileFilter toFileFilter() {
      return switch (type) {
        case "exclude"-> new FileExcludeFilter(expressions);
        case "include"-> new FileIncludeFilter(expressions);
        case "extension"-> new FileExtensionFilter(expressions);
        default -> throw new IllegalStateException("Unexpected filter type: " + type);
      };

    }
  }

}
