package sourceconnector.repository.file

import offsetmanager.domain.file.FileKey
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response
import sourceconnector.repository.file.S3Location.Companion.from
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
  override fun listFiles(vararg inputFileKeys: FileKey): List<FileKey> {
    val fileKeys: MutableList<FileKey> = mutableListOf()
    for (fileKey in inputFileKeys) {
      val s3Location = from(fileKey)

      val request = ListObjectsV2Request.builder()
        .bucket(s3Location.bucket)
        .prefix(s3Location.key)
        .delimiter("/")
        .build()

      val keys = this.listFilesInResponse(fileKey, request)

      fileKeys.addAll(keys)
    }

    return fileKeys
  }

  override fun listFilesRecursively(vararg inputFileKeys: FileKey): List<FileKey> {
    val fileKeys: MutableList<FileKey> = mutableListOf()
    for (fileKey in inputFileKeys) {
      val s3Location = from(fileKey)
      val request = ListObjectsV2Request.builder()
        .bucket(s3Location.bucket)
        .prefix(s3Location.key)
        .build()

      val keys = this.listFilesInResponse(fileKey, request)

      fileKeys.addAll(keys)
    }

    return fileKeys
  }

  private fun listFilesInResponse(fileKey: FileKey, request: ListObjectsV2Request): List<FileKey> {
    val s3Location = from(fileKey)

    return this.s3Client.listObjectsV2Paginator(request)
      .flatMap { response: ListObjectsV2Response? -> response!!.contents() }
      .map { s3Object -> S3Location(s3Location.bucket, s3Object.key()).toFileKey() }
      .filter { fileKey -> fileValidator.isValid(fileKey) }
      .toList()
  }
}
