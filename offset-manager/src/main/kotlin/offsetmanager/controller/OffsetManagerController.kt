package offsetmanager.controller

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size
import offsetmanager.api.v1.dto.LastOffsetRecordBatchRequest
import offsetmanager.api.v1.dto.LastOffsetRecordBatchResponse
import offsetmanager.api.v1.dto.LastOffsetRecordResponse
import offsetmanager.service.OffsetManagerService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("/api")
class OffsetManagerController(
  private val offsetManagerService: OffsetManagerService
) {

  @GetMapping(value = ["/offset-records"], version = "v1")
  fun getLastOffsetRecord(
    @NotEmpty @Size(
      min = 5,
      message = "Key must be at least 5 length"
    ) @RequestParam("key") key: @NotEmpty @Size(min = 5, message = "Key must be at least 5 length") String?
  ): LastOffsetRecordResponse {
    return this.offsetManagerService.readLastOffset(key!!)
  }

  @PostMapping(value = ["/offset-records"], version = "v1")
  fun getLastOffsetRecords(
    @RequestBody request: LastOffsetRecordBatchRequest
  ): LastOffsetRecordBatchResponse {
    return this.offsetManagerService.readLastOffsets(request.keys)
  }
}
