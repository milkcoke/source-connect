package sourceconnector.repository.file.filter;

@FunctionalInterface
public interface FileFilter {
  boolean accept(String filePath);
}
