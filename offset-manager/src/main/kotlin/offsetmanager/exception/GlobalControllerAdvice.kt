package offsetmanager.exception

import jakarta.validation.ConstraintViolationException
import jakarta.validation.Path
import offsetmanager.error.ErrorResponse
import offsetmanager.error.ErrorType
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalControllerAdvice {
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(OffsetNotFoundException::class)
  fun handleOffsetNotFoundRequest(exception: OffsetNotFoundException): ErrorResponse {
    return ErrorResponse.of(
      ErrorType.OFFSET_NOT_FOUND,
      exception.message!!
    )
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleInvalidRequest(ex: MethodArgumentNotValidException): ErrorResponse {
    val properties: MutableMap<String, String?> = HashMap()
    val fieldErrors = ex.bindingResult.fieldErrors

    for (error in fieldErrors) {
      properties[error.field] = error.defaultMessage
    }

    return ErrorResponse.of(
      ErrorType.INVALID_PARAMETER,
      "Invalid parameters",
      properties
    )
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(ConstraintViolationException::class)
  fun handleInvalidRequest(ex: ConstraintViolationException): ErrorResponse {
    val properties: MutableMap<String, String?> = HashMap()
    for (violation in ex.constraintViolations) {
      val parameterName = getPropertyName(violation.propertyPath)
      properties[parameterName!!] = violation.message
    }

    return ErrorResponse.of(
      ErrorType.INVALID_PARAMETER,
      "Invalid parameters",
      properties
    )
  }

  @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
  @ExceptionHandler(OffsetManagerNotReadyException::class)
  fun handleOffsetManagerNotReadyState(ex: OffsetManagerNotReadyException): ErrorResponse {
    return ErrorResponse.of(
      ErrorType.OFFSET_MANAGER_NOT_READY,
      "Offset storage is still initializing. Please retry later."
    )
  }

  private fun getPropertyName(path: Path): String? {
    var last: Path.Node? = null
    for (node in path) last = node
    checkNotNull(last)
    return last.name
  }
}
