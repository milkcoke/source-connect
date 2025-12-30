package sourceconnector.domain.pipeline

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.file.LocalFileKey.Companion.from
import org.assertj.core.api.Assertions
import org.assertj.core.api.ThrowableAssert
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import sourceconnector.domain.log.Log
import sourceconnector.domain.log.factory.JSONLogFactory
import sourceconnector.domain.pipeline.factory.FileBaseLogPipelineBuilder
import sourceconnector.exception.FileLogReadException
import sourceconnector.repository.file.LocalFileRepository
import java.nio.file.Path

internal class FileBaseLogPipelineTest {
  @DisplayName("Should throw IllegalArgumentException when the given offset is negative")
  @Test
  fun negativePositionTest() {
    // given
    val builder = FileBaseLogPipelineBuilder()
    val localPath = Path.of("src/test/resources/sample-data/empty-included.ndjson")
    val fileKey: FileKey = from(localPath)
    val pipeline = builder.createWithNoProcessor(LocalFileRepository(), fileKey, JSONLogFactory())

    // when then
    Assertions.assertThatThrownBy { pipeline.toPosition(-1) }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("Offset should be greater or equal to zero")
  }

  @DisplayName("Should start first line when the given offset is zero")
  @Test
  fun zeroOffsetPositionTest() {
    // given
    val builder = FileBaseLogPipelineBuilder()
    val localPath = Path.of("src/test/resources/sample-data/empty-included.ndjson")
    val fileKey: FileKey = from(localPath)
    val pipeline = builder.createWithNoProcessor(LocalFileRepository(), fileKey, JSONLogFactory())

    // when
    pipeline.toPosition(0)
    val log: Log = pipeline.getResult()!!

    // then
    Assertions.assertThat(log.get()).isEqualToIgnoringWhitespace(
      """
    {"status":"active","name":{"first":"Jeff","middle":"Rory","last":"Considine"},"username":"Jeff-Considine","password":"ozf0xXWPtbgmOpw","emails":["Jefferey84@example.com","Marilou_Feest30@gmail.com"],"phoneNumber":"258-978-5839 x51654","location":{"street":"5546 Cummings Flats","city":"Leslyfurt","state":"Wisconsin","country":"Macao","zip":"50914-3964","coordinates":{"latitude":42.5652,"longitude":32.0713}},"website":"https://lazy-stove.biz/","domain":"frightened-biplane.info","job":{"title":"Internal Applications Engineer","descriptor":"Chief","area":"Mobility","type":"Engineer","company":"Rogahn, Kreiger and Leannon"},"creditCard":{"number":"6771-8913-9997-5701","cvv":"110","issuer":"visa"},"uuid":"0a5a748a-ecc2-4bcf-af83-6d60192f9cd9","objectId":"65fef7675054562031df9448"}
    
    """.trimIndent()
    )
  }

  @DisplayName("Should start from the given offset")
  @Test
  fun offsetPositionTest() {
    // given
    val builder = FileBaseLogPipelineBuilder()
    val localPath = Path.of("src/test/resources/sample-data/empty-included.ndjson")
    val fileKey: FileKey = from(localPath)
    val pipeline = builder.createWithNoProcessor(LocalFileRepository(), fileKey, JSONLogFactory())
    // when
    pipeline.toPosition(1)
    val log: Log = pipeline.getResult()!!
    // then
    Assertions.assertThat(log.get()).isEqualToIgnoringWhitespace(
      """
    {"status":"active","name":{"first":"Kade","middle":"Nico","last":"Osinski"},"username":"Kade-Osinski","password":"8CqLZzFIbMxyA7s","emails":["Lucas_Rempel-Quigley58@gmail.com","Shany_Bruen@example.com"],"phoneNumber":"(392) 545-1550 x6706","location":{"street":"9571 Grimes Forges","city":"Marvinside","state":"Oklahoma","country":"Eswatini","zip":"10727-0485","coordinates":{"latitude":-1.0158,"longitude":-126.0164}},"website":"https://lighthearted-mantua.biz","domain":"plaintive-miss.biz","job":{"title":"Chief Identity Executive","descriptor":"Forward","area":"Mobility","type":"Consultant","company":"Stark, White and Wisozk"},"creditCard":{"number":"6560-6217-6613-3496-4263","cvv":"251","issuer":"maestro"},"uuid":"93345f80-bf80-4f9d-81a0-2067d9902598","objectId":"65fef7675054562031df9449"}
    
    """.trimIndent()
    )
  }

  @DisplayName("Should throw IllegalArgumentException when the given offset exceeds file length")
  @Test
  fun exceedOffsetTest() {
    // given
    val builder = FileBaseLogPipelineBuilder()
    val localPath = Path.of("src/test/resources/sample-data/empty-included.ndjson")
    val fileKey: FileKey = from(localPath)
    val pipeline = builder.createWithNoProcessor(LocalFileRepository(), fileKey, JSONLogFactory())
    // when then
    Assertions.assertThatThrownBy { pipeline.toPosition(18) }
      .isInstanceOf(FileLogReadException::class.java)
      .hasMessageStartingWith("Offset: 18 exceeds file length in file:")
  }
}
