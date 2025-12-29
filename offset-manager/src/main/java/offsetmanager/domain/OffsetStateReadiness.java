package offsetmanager.domain;

/**
 * Indicates readiness of the OffsetStateUpdater <br>
 * External clients should call {@link #awaitReady()} before querying offset state
 */
public interface OffsetStateReadiness {
  void awaitReady();
}
