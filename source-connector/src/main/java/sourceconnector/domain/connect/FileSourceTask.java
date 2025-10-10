package sourceconnector.domain.connect;

import lombok.AccessLevel;
import lombok.Getter;
import sourceconnector.repository.file.FileRepository;

import java.util.ArrayList;
import java.util.List;

public class FileSourceTask implements Task<FileProcessingResult> {
  private final String id;
  @Getter
  private final int index;
  private final FileRepository fileRepository;

  // visible for test
  @Getter(AccessLevel.PACKAGE)
  private final List<String> filePaths = new ArrayList<>();
  private final FileProcessingResult result = new FileProcessingResult();

  public FileSourceTask(int index, FileRepository fileRepository) {
    this.id = String.format("Task-%d", index);
    this.index = index;
    this.fileRepository = fileRepository;
  }

  //TODO: implement this
  @Override
  public FileProcessingResult call() throws Exception {
    return this.result;
  }

  @Override
  public void assign(List<String> filePathList) {
    filePaths.addAll(filePathList);
    this.result.setTotalCount(filePaths.size());
  }
}
