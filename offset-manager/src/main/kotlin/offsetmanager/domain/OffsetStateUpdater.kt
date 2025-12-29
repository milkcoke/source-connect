package offsetmanager.domain;

/**
 * Synchronizes {@link offsetmanager.domain.offset.OffsetRecord} state from external systems (e.g. Kafka)
 */
public interface OffsetStateUpdater extends OffsetStateReadiness {
  void start();
  void stop();
}
