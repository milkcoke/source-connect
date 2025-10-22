package sourceconnector.config;

import java.util.List;

public record S3Config(
  List<String> paths,
  String bucket,
  String region
) {
}
