package offsetmanager.domain

/**
 * Indicates readiness of the OffsetStateUpdater <br></br>
 * External clients should call [.awaitReady] before querying offset state
 */
interface OffsetStateReadiness {
  fun awaitReady()
}
