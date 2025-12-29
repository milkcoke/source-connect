package offsetmanager.domain

/**
 * Synchronizes [offsetmanager.domain.offset.OffsetRecord] state from external systems (e.g. Kafka)
 */
interface OffsetStateUpdater : OffsetStateReadiness {
  fun start()
  fun stop()
}
