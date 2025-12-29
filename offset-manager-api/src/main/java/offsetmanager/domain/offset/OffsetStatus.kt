package offsetmanager.domain.offset

enum class OffsetStatus(val offset: Long) {
  INITIAL(0L),
  COMPLETED(-1L);
}
