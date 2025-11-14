package sourceconnector.domain.pipeline.factory;

import sourceconnector.domain.file.FileKey;
import sourceconnector.domain.pipeline.Pipeline;

/**
 * Provides simple factory API to create {@link Pipeline} instance. <br>
 * Pipeline should be created per file handled by the {@link sourceconnector.domain.connect.Task}
 */
@FunctionalInterface
public interface PipelineSupplier<T> {
  Pipeline<T> get(FileKey fileKey);
}
