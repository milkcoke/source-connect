package sourceconnector.domain.offset;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OffsetStatus {
  INITIAL_OFFSET(0L),
  COMPLETE_OFFSET(-1L);

  private final long value;
}
