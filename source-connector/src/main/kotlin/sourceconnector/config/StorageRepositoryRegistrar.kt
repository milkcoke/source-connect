package sourceconnector.config

import aws.sdk.kotlin.services.s3.S3Client
import org.springframework.beans.factory.BeanRegistrar
import org.springframework.beans.factory.BeanRegistry
import org.springframework.core.env.Environment
import sourceconnector.repository.file.*
import java.util.*

class StorageRepositoryRegistrar: BeanRegistrar {
  override fun register(registry: BeanRegistry, env: Environment) {
    val storageType = env.getProperty("source.storage.type")
    requireNotNull(storageType) { "source.storage.type should be set" }

    when (storageType.lowercase(Locale.getDefault())) {
      "local" -> {
        registry.registerBean<LocalFileRepository>(
          "delegateFileRepository",
          LocalFileRepository::class.java
        )
        registry.registerBean<LocalFileLister>(
          "fileLister",
          LocalFileLister::class.java
        )
      }

      "s3" -> {
        registry.registerBean<S3Client>(
          "s3Client",
          S3Client::class.java
        ) { spec: BeanRegistry.Spec<S3Client> ->
          spec.supplier { context: BeanRegistry.SupplierContext ->
            S3Client {
              region = context.bean<S3Config>(S3Config::class.java).region
            }
          }
        }
        registry.registerBean<S3FileRepository>(
          "delegateFileRepository",
          S3FileRepository::class.java
        )
        registry.registerBean<S3FileLister>(
          "fileLister",
          S3FileLister::class.java
        )
      }

      else -> throw IllegalArgumentException("Unknown storage type $storageType")
    }

    registry.registerBean<DecompressingFileRepository>(
      "fileRepository",
      DecompressingFileRepository::class.java
    ) { spec: BeanRegistry.Spec<DecompressingFileRepository> ->
      spec
        .primary()
        .supplier { context: BeanRegistry.SupplierContext ->
          DecompressingFileRepository(
            context.bean<FileRepository>(
              "delegateFileRepository",
              FileRepository::class.java
            )
          )
        }
    }
  }
}
