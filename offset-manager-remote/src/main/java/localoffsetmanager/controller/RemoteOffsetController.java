package localoffsetmanager.controller;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import localoffsetmanager.controller.dto.LastOffsetRecordBatchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import localoffsetmanager.service.RemoteOffsetService;
import localoffsetmanager.service.dto.LastOffsetRecordBatchResponse;
import localoffsetmanager.service.dto.LastOffsetRecordResponse;


@Validated
@RestController
@RequiredArgsConstructor
public class RemoteOffsetController {
  private final RemoteOffsetService remoteOffsetService;

  @GetMapping("/v1/offset-records")
  public LastOffsetRecordResponse getLastOffsetRecord(
    @NotEmpty
    @Size(min = 5, message = "Key must be at least 5 length")
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
