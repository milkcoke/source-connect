package offsetmanager.controller;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import offsetmanager.controller.dto.LastOffsetRecordBatchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import offsetmanager.service.OffsetManagerService;
import offsetmanager.service.dto.LastOffsetRecordBatchResponse;
import offsetmanager.service.dto.LastOffsetRecordResponse;


@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class OffsetManagerController {
  private final OffsetManagerService offsetManagerService;

  @GetMapping(value = "/offset-records", version = "v1")
  public LastOffsetRecordResponse getLastOffsetRecord(
    @NotEmpty
    @Size(min = 5, message = "Key must be at least 5 length")
    @RequestParam("key")
    String key
  ) {
    return this.offsetManagerService.readLastOffset(key);
  }

  @PostMapping(value = "/offset-records", version = "v1")
  public LastOffsetRecordBatchResponse getLastOffsetRecords(
    @RequestBody
    LastOffsetRecordBatchRequest request
  ) {
    return this.offsetManagerService.readLastOffsets(request.keys());
  }
}
