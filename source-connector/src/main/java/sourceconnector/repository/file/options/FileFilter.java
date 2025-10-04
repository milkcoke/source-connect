package sourceconnector.repository.file.options;

@FunctionalInterface
public interface FileFilter {
  boolean accept(String filePath);
}
