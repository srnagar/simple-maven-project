package io.sample.maven.project.trace.eventhubs;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.LoadBalancingStrategy;
import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;

import java.util.concurrent.CountDownLatch;

public class CollectEventHubsTraceAgent {
    private static final String STORAGE_CONNECTION_STRING = "";
    private static final String EVENTHUBS_CONNECTION_STRING = "";

    public static void main(String[] args) throws InterruptedException {
        sendEvents();
        BlobContainerAsyncClient blobContainerAsyncClient = new BlobContainerClientBuilder()
                .connectionString(STORAGE_CONNECTION_STRING)
                .containerName("eh-tests")
                .buildAsyncClient();

        CountDownLatch latch = new CountDownLatch(1);

        EventProcessorClient processorClient = new EventProcessorClientBuilder()
                .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
                .connectionString(EVENTHUBS_CONNECTION_STRING)
                .processEvent(event -> {
                    System.out.println("Received event");
                    event.updateCheckpoint();
                    latch.countDown();
                })
                .processError(error -> System.out.println("Error handler triggered" + error.getThrowable().getMessage()))
                .loadBalancingStrategy(LoadBalancingStrategy.GREEDY)
                .checkpointStore(new BlobCheckpointStore(blobContainerAsyncClient))
                .buildEventProcessorClient();

        processorClient.start();
        System.out.println("Processor started");
        latch.await();
        Thread.sleep(20000);
        processorClient.stop();

        System.out.println("Processor stopped");

        System.out.println("Complete");
    }

    private static void sendEvents() {
        EventHubProducerAsyncClient producer = new EventHubClientBuilder()
                .connectionString(EVENTHUBS_CONNECTION_STRING)
                .buildAsyncProducerClient();

        // send an event after partition 0 is owned
        producer.createBatch(new CreateBatchOptions().setPartitionId("0"))
                .flatMap(batch -> {
                    batch.tryAdd(new EventData("test event"));
                    return producer.send(batch);
                }).block();
        System.out.println("Sent event");

        producer.close();
    }
}
