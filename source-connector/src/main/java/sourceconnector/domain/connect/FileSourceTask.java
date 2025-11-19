package sourceconnector.domain.connect;

import lombok.AccessLevel;
import lombok.Getter;
import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.offset.OffsetStatus;
import sourceconnector.domain.log.Log;
import sourceconnector.domain.log.LogMetadata;
import sourceconnector.domain.offset.LocalFileOffsetRecord;
import sourceconnector.domain.pipeline.factory.PipelineSupplier;
import sourceconnector.service.batcher.LogBatcher;
import sourceconnector.domain.pipeline.Pipeline;
import sourceconnector.service.producer.BatchProducer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileSourceTask implements Task<FileProcessingResult> {
  private final String id;
  @Getter
  private final int index;
  private final BatchProducer<String> producer;
  private final PipelineSupplier<Log> pipelineSupplier;

  // visible for test
  @Getter(AccessLevel.PACKAGE)
  private final List<FileKey> fileKeys = new ArrayList<>();
  private final FileProcessingResult result = new FileProcessingResult();

  public FileSourceTask(
    int index,
    PipelineSupplier<Log> pipelineSupplier,
    BatchProducer<String> producer
  ) {
    this.id = String.format("Task-%d", index);
    this.index = index;
    this.pipelineSupplier = pipelineSupplier;
    this.producer = producer;
  }

  @Override
  public FileProcessingResult call() throws Exception {
    try {
      for (var fileKey: this.fileKeys) {
        Pipeline<Log> pipeline = pipelineSupplier.get(fileKey);

        LogBatcher batcher = new LogBatcher(pipeline, 10_000);

        LogMetadata lastMessageMetadata;

        while (batcher.hasNextBatch()) {
          List<Log> messages = batcher.nextBatch().get();
          if (messages.isEmpty()) continue;
          lastMessageMetadata = messages.getLast().getMetadata();
          List<String> messageBatch = messages
            .stream()
            .map(Log::get)
            .toList();

          producer.sendBatch(
            new LocalFileOffsetRecord(
              lastMessageMetadata.key(),
              lastMessageMetadata.offset()
            ),
            ()->messageBatch
          );
        }


        // Complete this file
        producer.sendBatch(
          new LocalFileOffsetRecord(
            // This is for handling no Log after filtered
            fileKey.get(),
            OffsetStatus.COMPLETED.getValue()
          ),
          Collections::emptyList
        );

        this.result.addSuccessCount();
      }
      return this.result;
    } finally {
      this.producer.close();
    }
  }

  @Override
  public void assign(List<FileKey> fileKeys) {
    this.fileKeys.addAll(fileKeys);
    this.result.setTotalCount(this.fileKeys.size());
  }
}
