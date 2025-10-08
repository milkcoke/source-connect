package sourceconnector.domain.connect;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileSourceTask implements Task<FileProcessingResult>{
  private final FileProcessingResult result;

  @Override
  public FileProcessingResult call() throws Exception {
    return null;
  }
}
