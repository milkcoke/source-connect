package sourceconnector.repository.file.filter;

import offsetmanager.domain.file.FileKey;

import java.util.List;
import java.util.regex.Pattern;

public class FileExcludeFilter implements FileFilter {
  private final List<Pattern> patterns;

  public FileExcludeFilter(List<String> regexExpressions) {
    if (regexExpressions == null || regexExpressions.isEmpty()) {
      throw new IllegalArgumentException("regexExpressions cannot be null or empty");
    }
    this.patterns = regexExpressions.stream()
      .map(Pattern::compile)
      .toList();
  }

  @Override
  public boolean accept(FileKey fileKey) {
    return patterns.stream().noneMatch(regex -> regex.matcher(fileKey.get()).find());
  }
}
