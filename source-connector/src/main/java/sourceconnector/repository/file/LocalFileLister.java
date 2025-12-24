package sourceconnector.repository.file;

import lombok.RequiredArgsConstructor;
import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.file.LocalFileKey;
import sourceconnector.repository.file.validator.FileValidator;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class LocalFileLister implements FileLister {
  private final FileValidator fileValidator;

  @Override
  public List<FileKey> listFiles(FileKey... fileKeys) {

    if (fileKeys == null || fileKeys.length == 0) {
      throw new IllegalArgumentException("paths cannot be null or empty");
    }

    List<FileKey> result = new ArrayList<>();

    for (FileKey fileKey : fileKeys) {
      Path absolutePath = Path.of(URI.create(fileKey.get())).toAbsolutePath();
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
  public List<FileKey> listFilesRecursively(FileKey... fileKeys) {
    if (fileKeys == null || fileKeys.length == 0) {
      throw new IllegalArgumentException("paths cannot be null or empty");
    }

    List<FileKey> result = new ArrayList<>();
    for (FileKey fileKey : fileKeys) {
      Path absolutePath = Path.of(URI.create(fileKey.get())).toAbsolutePath();
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

  private List<FileKey> handleFile(Path absFilePath) {
    return fileValidator.isValid(LocalFileKey.from(absFilePath))
      ? List.of(LocalFileKey.from(absFilePath))
      : Collections.emptyList();
  }


  private List<FileKey> handleDirectory(Path absDir) {
    try (Stream<Path> stream = Files.list(absDir)) {
      return stream
        .filter(Files::isRegularFile)
        .map(LocalFileKey::from)
        .filter(fileValidator::isValid)
        .collect(Collectors.toUnmodifiableList());
    } catch (IOException ex) {
      throw new IllegalArgumentException("failed to list files in directory: " + absDir);
    }
  }
  private List<FileKey> handleDirectoryRecursively(Path absDir) {
    try (Stream<Path> stream = Files.walk(absDir, Integer.MAX_VALUE)) {
      return stream
        .filter(Files::isRegularFile)
        .map(LocalFileKey::from)
        .filter(fileValidator::isValid)
        .collect(Collectors.toUnmodifiableList());
    } catch (IOException ex) {
      throw new IllegalArgumentException("failed to list files in directory: " + absDir);
    }
  }

}
