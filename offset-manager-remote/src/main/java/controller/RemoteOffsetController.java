package controller;

import controller.dto.LastOffsetRecordBatchRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import service.RemoteOffsetService;
import service.dto.LastOffsetRecordBatchResponse;
import service.dto.LastOffsetRecordResponse;


@RestController
@RequiredArgsConstructor
public class RemoteOffsetController {
  private final RemoteOffsetService remoteOffsetService;

  @GetMapping("/v1/offset-records")
  public LastOffsetRecordResponse getLastOffsetRecord(
    @NotEmpty
    @Size(min = 5, message = "Key must be at least 3 length")
    @RequestParam("key")
    String key
  ) {
    return this.remoteOffsetService.readLastOffset(key);
  }

  @PostMapping("/v1/offset-records")
  public LastOffsetRecordBatchResponse getLastOffsetRecords(
    @RequestBody
    LastOffsetRecordBatchRequest request
  ) {
    return this.remoteOffsetService.readLastOffsets(request.keys());
  }
}
