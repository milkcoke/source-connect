package sourceconnector.domain.connect;

import lombok.AccessLevel;
import lombok.Getter;
import offsetmanager.domain.OffsetStatus;
import sourceconnector.domain.factory.JSONLogFactory;
import sourceconnector.domain.log.Log;
import sourceconnector.domain.log.LogMetadata;
import sourceconnector.domain.offset.LocalFileOffsetRecord;
import sourceconnector.repository.file.FileRepository;
import sourceconnector.service.batcher.LogBatcher;
import sourceconnector.service.pipeline.FileBaseLogPipeline;
import sourceconnector.service.pipeline.Pipeline;
import sourceconnector.service.processor.impl.EmptyFilterProcessor;
import sourceconnector.service.processor.impl.TrimMapperProcessor;
import sourceconnector.service.producer.BatchProducer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileSourceTask implements Task<FileProcessingResult> {
  private final String id;
  @Getter
  private final int index;
  private final FileRepository fileRepository;
  private final BatchProducer<String> producer;

  // visible for test
  @Getter(AccessLevel.PACKAGE)
  private final List<String> filePaths = new ArrayList<>();
  private final FileProcessingResult result = new FileProcessingResult();

  public FileSourceTask(int index, FileRepository fileRepository, BatchProducer<String> producer) {
    this.id = String.format("Task-%d", index);
    this.index = index;
    this.fileRepository = fileRepository;
    this.producer = producer;
  }

  @Override
  public FileProcessingResult call() {
    for (var filePath: this.filePaths) {
      Pipeline<Log> pipeline = FileBaseLogPipeline.create(
        this.fileRepository,
        filePath,
        new JSONLogFactory(),
        new EmptyFilterProcessor(),
        new TrimMapperProcessor(new JSONLogFactory())
      );

      LogBatcher batcher = new LogBatcher(pipeline, 10_000);

      List<Log> messages;

      LogMetadata lastMessageMetadata;

      while ((messages = batcher.nextBatch().get()) != Collections.EMPTY_LIST) {
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
          filePath,
          OffsetStatus.COMPLETED.getValue()
        ),
        Collections::emptyList
      );

      this.result.addSuccessCount();
    }

    return this.result;
  }

  @Override
  public void assign(List<String> filePathList) {
    filePaths.addAll(filePathList);
    this.result.setTotalCount(filePaths.size());
  }
}
