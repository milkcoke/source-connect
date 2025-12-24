package offsetmanager.domain.file.factory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.file.LocalFileKey;
import offsetmanager.domain.file.S3Uri;

import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public enum FileKeyParser {
  LOCAL("file:///" , filePath -> LocalFileKey.from(Path.of(URI.create(filePath)))),
  S3("s3://", s3Uri -> S3Uri.from(s3Uri).toFileKey());

  private final String prefix;
  @Getter
  private final Function<String, FileKey> parser;
  private final static Map<String, Function<String, FileKey>> PREFIX_MAP = Arrays.stream(values())
    .collect(Collectors.toMap(
      parser -> parser.prefix,
      parser -> parser.parser
    ));

  public static FileKey parse(String keyString) {
    for  (String prefix : PREFIX_MAP.keySet()) {
      if (keyString.startsWith(prefix)) {
        var parser = PREFIX_MAP.get(prefix);
        return parser.apply(keyString);
      }
    }
    throw new IllegalArgumentException("Unsupported file key schema: " + keyString);
  }
}
