package sourceconnector.domain.pipeline.factory;

import sourceconnector.domain.log.Log;
import sourceconnector.domain.log.factory.LogFactory;
import sourceconnector.domain.pipeline.Pipeline;
import sourceconnector.repository.file.FileRepository;

@FunctionalInterface
public interface PipelineSupplier {
  Pipeline<Log> get(FileRepository fileRepository, String filePath, LogFactory logFactory);
}
