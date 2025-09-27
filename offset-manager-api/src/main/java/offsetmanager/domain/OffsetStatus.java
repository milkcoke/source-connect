package offsetmanager.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OffsetStatus {
  INITIAL(0L),
  COMPLETED(-1L);

  private final long value;
}
