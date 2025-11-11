package sourceconnector.repository.file;

import lombok.RequiredArgsConstructor;
import sourceconnector.repository.file.validator.FileValidator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class LocalFileLister implements FileLister {
  private final FileValidator fileValidator;

  @Override
  public List<String> listFiles(String... paths) {

    if (paths == null || paths.length == 0) {
      throw new IllegalArgumentException("paths cannot be null or empty");
    }

    List<String> result = new ArrayList<>();

    for (String path : paths) {
      Path absolutePath = Path.of(path).toAbsolutePath();
      this.validatePathExists(absolutePath);
      if (Files.isRegularFile(absolutePath)) {
        result.addAll(this.handleFile(absolutePath));
      } else if (Files.isDirectory(absolutePath)) {
        result.addAll(this.handleDirectory(absolutePath));
      }
    }
    return result;
  }

  @Override
  public List<String> listFilesRecursively(String... paths) {
    if (paths == null || paths.length == 0) {
      throw new IllegalArgumentException("paths cannot be null or empty");
    }

    List<String> result = new ArrayList<>();
    for (String path : paths) {
      Path absolutePath = Path.of(path).toAbsolutePath();
      this.validatePathExists(absolutePath);
      if (Files.isRegularFile(absolutePath)) {
        result.addAll(this.handleFile(absolutePath));
      } else if (Files.isDirectory(absolutePath)) {
        result.addAll(this.handleDirectoryRecursively(absolutePath));
      }
    }

    return result;
  }

  private void validatePathExists(Path absPath) {
    if (!Files.exists(absPath)) {
      throw new IllegalArgumentException("path does not exist: " + absPath);
    }
  }

  private List<String> handleFile(Path absFilePath) {
    return fileValidator.isValid(absFilePath.toString())
      ? List.of(absFilePath.toString())
      : Collections.emptyList();
  }


  private List<String> handleDirectory(Path absDir) {
    try (Stream<Path> stream = Files.list(absDir)) {
      return stream
        .filter(Files::isRegularFile)
        .map(path -> path.toAbsolutePath().toString())
        .filter(fileValidator::isValid)
        .toList();
    } catch (IOException ex) {
      throw new IllegalArgumentException("failed to list files in directory: " + absDir);
    }
  }
  private List<String> handleDirectoryRecursively(Path absDir) {
    try (Stream<Path> stream = Files.walk(absDir, Integer.MAX_VALUE)) {
      return stream
        .filter(Files::isRegularFile)
        .map(path -> path.toAbsolutePath().toString())
        .filter(fileValidator::isValid)
        .toList();
    } catch (IOException ex) {
      throw new IllegalArgumentException("failed to list files in directory: " + absDir);
    }
  }

}
