package sourceconnector.repository.file.validator;

import offsetmanager.domain.file.FileKey;

public class NoConditionFileValidator implements FileValidator {
  @Override
  public boolean isValid(FileKey filePath) {
    return true;
  }
}
