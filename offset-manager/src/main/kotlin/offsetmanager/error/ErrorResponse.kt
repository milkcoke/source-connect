package offsetmanager.error

import com.fasterxml.jackson.annotation.JsonInclude

data class ErrorResponse(
  val statusCode: Int,
  val type: String,
  val message: String,
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  val properties: Map<String, String?>
) {
  companion object {
    @JvmStatic
    fun of(errorType: ErrorType, message: String): ErrorResponse {
      return ErrorResponse(
        errorType.httpStatusCode,
        errorType.name,
        message,
        mapOf()
      )
    }

    @JvmStatic
    fun of(
      errorType: ErrorType,
      message: String,
      properties: MutableMap<String, String?>
    ): ErrorResponse {
      return ErrorResponse(
        errorType.httpStatusCode,
        errorType.name,
        message,
        properties
      )
    }
  }
}
