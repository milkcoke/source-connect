package sourceconnector.repository.file

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.S3Exception
import aws.smithy.kotlin.runtime.content.toInputStream
import kotlinx.coroutines.runBlocking
import offsetmanager.domain.file.FileKey
import java.io.ByteArrayInputStream
import java.io.InputStream


class S3FileRepository(
  private val s3Client: S3Client
) : FileRepository {

  override fun getFile(fileKey: FileKey): InputStream {
    return runBlocking {
      val s3Location = S3Location.from(fileKey)
      val request = GetObjectRequest {
        bucket  = s3Location.bucket
        key = s3Location.key
      }

      try {
        s3Client.getObject(request) {response ->
          response.body?.toInputStream()
            ?: ByteArrayInputStream(byteArrayOf())
        }
      } catch (e: S3Exception){
        throw RuntimeException("Failed to get file from: ${fileKey.get()}", e)
      }
    }
  }
}
