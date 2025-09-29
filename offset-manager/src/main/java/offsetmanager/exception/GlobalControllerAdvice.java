package offsetmanager.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.Path.Node;
import offsetmanager.error.ErrorResponse;
import offsetmanager.error.ErrorType;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalControllerAdvice {

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(OffsetNotFoundException.class)
  public ErrorResponse handleOffsetNotFoundRequest(OffsetNotFoundException exception) {
    return ErrorResponse.of(
          ErrorType.OFFSET_NOT_FOUND,
          exception.getMessage()
    );
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ErrorResponse handleInvalidRequest(MethodArgumentNotValidException ex) {
    Map<String, String> properties = new HashMap<>();
    List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

    for (FieldError error : fieldErrors) {
      properties.put(error.getField(), error.getDefaultMessage());
    }

    return ErrorResponse.of(
      ErrorType.INVALID_PARAMETER,
      "Invalid parameters",
      properties
    );
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(ConstraintViolationException.class)
  public ErrorResponse handleInvalidRequest(ConstraintViolationException ex) {
    Map<String, String> properties = new HashMap<>();
    for (var violation : ex.getConstraintViolations()) {
      String parameterName = getPropertyName(violation.getPropertyPath());
      properties.put(parameterName, violation.getMessage());
    }

    return ErrorResponse.of(
      ErrorType.INVALID_PARAMETER,
      "Invalid parameters",
      properties
    );
  }

  private String getPropertyName(Path path) {
    Node last = null;
    for (Node node : path) last = node;
    assert last != null;
    return last.getName();
  }
}
