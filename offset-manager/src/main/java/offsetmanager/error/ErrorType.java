package offsetmanager.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Getter
@RequiredArgsConstructor
public enum ErrorType {
  INVALID_PARAMETER(BAD_REQUEST.value(), "Invalid parameter"),
  OFFSET_NOT_FOUND(NOT_FOUND.value(), "Offset not found");

  private final int httpStatusCode;
  private final String message;
}
