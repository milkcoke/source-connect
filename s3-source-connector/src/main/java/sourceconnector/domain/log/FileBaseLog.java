package sourceconnector.domain.log;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class FileBaseLog implements Log {
  private final FileMetadata metadata;
}
