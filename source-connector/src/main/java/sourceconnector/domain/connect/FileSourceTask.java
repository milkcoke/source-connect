package sourceconnector.domain.connect;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sourceconnector.repository.file.FileRepository;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class FileSourceTask implements Task<FileProcessingResult>{
  private final String id;
  @Getter
  private final int index;
  private final FileRepository fileRepository;

  // visible for test
  @Getter(AccessLevel.PACKAGE)
  private final List<String> filePaths = new ArrayList<>();
  private final FileProcessingResult result = new FileProcessingResult();

  @Override
  public FileProcessingResult call() throws Exception {
    return null;
  }

  @Override
  public void assign(List<String> filePathList) {
    filePaths.addAll(filePathList);
  }
}
