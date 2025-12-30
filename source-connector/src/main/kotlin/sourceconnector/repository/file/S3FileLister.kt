package sourceconnector.repository.file

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.ListObjectsV2Request
import aws.sdk.kotlin.services.s3.paginators.listObjectsV2Paginated
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import offsetmanager.domain.file.FileKey
import sourceconnector.repository.file.validator.FileValidator

class S3FileLister(
  private val s3Client: S3Client,
  private val fileValidator: FileValidator
) : FileLister {


  /**
   * Get all s3 object key paths <br></br>
   * this can handle both directory and file path
   * @return `List<String>`
   */
  override fun listFiles(vararg fileKeys: FileKey): List<FileKey> {
    return listS3Files(fileKeys, recursively = false)
  }

  override fun listFilesRecursively(vararg fileKeys: FileKey): List<FileKey> {
    return listS3Files(fileKeys, recursively = true)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  private fun listS3Files(
    inputFileKeys: Array<out FileKey>,
    recursively: Boolean
  ): List<FileKey> {
    return inputFileKeys.flatMap { fileKey ->
      runBlocking {
        val s3Location = S3Location.from(fileKey)
        val request = ListObjectsV2Request {
          bucket = s3Location.bucket
          prefix = s3Location.key
          if (!recursively) {
            delimiter = "/"
          }
        }

        s3Client.listObjectsV2Paginated(request)
          .flatMapConcat { response ->
            response.contents.orEmpty().asFlow()
          }
          .map { s3Object -> S3Location(s3Location.bucket, s3Object.key!!).toFileKey() }
          .filter { fileValidator.isValid(it) }
          .toList()
      }
    }
  }
}
