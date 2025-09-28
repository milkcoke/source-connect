package localoffsetmanager.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collections;
import java.util.Map;

public record ErrorResponse(
  int statusCode,
  String type,
  String message,
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  Map<String, String> properties
) {
  public static ErrorResponse of(ErrorType errorType, String message) {
    return new ErrorResponse(
      errorType.getHttpStatusCode(),
      errorType.name(),
      message,
      Collections.emptyMap()
    );
  }

  public static ErrorResponse of(
    ErrorType errorType,
    String message,
    Map<String, String> properties
  ) {
    return new ErrorResponse(
      errorType.getHttpStatusCode(),
      errorType.name(),
      message,
      properties
    );
  }
}
