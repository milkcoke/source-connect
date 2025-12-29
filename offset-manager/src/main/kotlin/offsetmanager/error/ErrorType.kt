package offsetmanager.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static org.springframework.http.HttpStatus.*;

@Getter
@RequiredArgsConstructor
public enum ErrorType {

  OFFSET_MANAGER_NOT_READY(SERVICE_UNAVAILABLE.value(), "Offset manager is not ready"),
  INVALID_PARAMETER(BAD_REQUEST.value(), "Invalid parameter"),
  OFFSET_NOT_FOUND(NOT_FOUND.value(), "Offset not found");

  private final int httpStatusCode;
  private final String message;
}
