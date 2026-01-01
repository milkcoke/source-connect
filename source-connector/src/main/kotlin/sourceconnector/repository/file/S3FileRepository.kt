package sourceconnector.repository.file

import offsetmanager.domain.file.FileKey
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.S3Exception
import sourceconnector.repository.file.S3Location.Companion.from
import java.io.InputStream


class S3FileRepository(
  private val s3Client: S3Client
) : FileRepository {
  override fun getFile(fileKey: FileKey): InputStream {
    val s3Location = from(fileKey)
    try {
      val request = GetObjectRequest.builder()
        .bucket(s3Location.bucket)
        .key(s3Location.key)
        .build()

      return s3Client.getObject(request)
    } catch (e: S3Exception) {
      throw RuntimeException("Failed to get file from: " + fileKey.get(), e)
    }
  }
}
