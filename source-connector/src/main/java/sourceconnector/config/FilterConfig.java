package sourceconnector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import sourceconnector.repository.file.filter.FileExcludeFilter;
import sourceconnector.repository.file.filter.FileExtensionFilter;
import sourceconnector.repository.file.filter.FileFilter;
import sourceconnector.repository.file.filter.FileIncludeFilter;

import java.util.List;

@ConfigurationProperties("app.storage.filters")
public record FilterConfig(
  String type,
  List<String> expressions
) {
  public FileFilter toFileFilter() {
    return switch (type) {
      case "exclude"-> new FileExcludeFilter(expressions);
      case "include"-> new FileIncludeFilter(expressions);
      case "extension"-> new FileExtensionFilter(expressions);
      default -> throw new IllegalStateException("Unexpected filter type: " + type);
    };
  }
}
