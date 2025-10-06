package sourceconnector.repository.file.validator;

public class NoConditionFileValidator implements FileValidator {
  @Override
  public boolean isValid(String filePath) {
    return true;
  }
}
