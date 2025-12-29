package sourceconnector.config;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.BeanRegistrar;
import org.springframework.beans.factory.BeanRegistry;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import sourceconnector.repository.file.*;

public class StorageRepositoryRegistrar implements BeanRegistrar {
  @Override
  public void register(@NonNull BeanRegistry registry, Environment env) {
    String storageType = env.getProperty("source.storage.type");
    if (storageType == null) {
      throw new IllegalArgumentException("source.storage.type should be set");
    }

    switch (storageType.toLowerCase()) {

      case "local"-> {
        registry.registerBean(
          "delegateFileRepository",
          LocalFileRepository.class
        );
        registry.registerBean(
          "fileLister",
          LocalFileLister.class
        );
      }

      case "s3" -> {
        registry.registerBean(
          "s3Client",
          S3Client.class,
          spec -> spec.supplier(context -> S3Client.builder()
            .region(Region.of(context.bean(S3Config.class).region()))
            .build())
        );
        registry.registerBean(
          "delegateFileRepository",
          S3FileRepository.class
        );
        registry.registerBean(
          "fileLister",
          S3FileLister.class
        );
      }

      default -> throw new IllegalArgumentException("Unknown storage type " + storageType);
    }

    registry.registerBean(
      "fileRepository",
      DecompressingFileRepository.class,
  spec -> spec
        .primary()
        .supplier(context -> new DecompressingFileRepository(context.bean("delegateFileRepository", FileRepository.class)))
    );
  }
}
