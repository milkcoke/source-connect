package offsetmanager.error

import org.springframework.http.HttpStatus

enum class ErrorType(
  val httpStatusCode: Int,
  val message: String
) {
  OFFSET_MANAGER_NOT_READY(HttpStatus.SERVICE_UNAVAILABLE.value(), "Offset manager is not ready"),
  INVALID_PARAMETER(HttpStatus.BAD_REQUEST.value(), "Invalid parameter"),
  OFFSET_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "Offset not found");
}
