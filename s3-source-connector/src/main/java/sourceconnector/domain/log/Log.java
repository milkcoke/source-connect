package sourceconnector.domain.log;

/**
 * Log Should have the payload and metadata
 */
public interface Log {
  String get();
  Metadata getMetadata();
}
