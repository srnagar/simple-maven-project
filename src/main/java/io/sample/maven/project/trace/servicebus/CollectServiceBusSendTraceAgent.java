package io.sample.maven.project.trace.servicebus;

import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.ServiceBusSessionReceiverClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;

import java.util.concurrent.CountDownLatch;

public class CollectServiceBusSendTraceAgent {

    /**
     * The main method to run the application.
     * @param args Ignored args.
     */
    public static void main(String[] args) throws InterruptedException {
        doClientWork();
        Thread.sleep(5000);
    }

    private static void doClientWork() throws InterruptedException {

        String serviceBusConnectionString = "";
        String[] sessions = {"soccer", "cricket", "tennis", "badminton"};
        ServiceBusSenderClient serviceBusSenderClient = new ServiceBusClientBuilder()
                .connectionString(serviceBusConnectionString)
                .sender()
                .buildClient();

        int i = 0;
        while (i < 4) {
            ServiceBusMessage message = new ServiceBusMessage("hello " + i)
                    .setMessageId(String.valueOf(i))
                    .setSessionId(sessions[i % 4]);
            serviceBusSenderClient.sendMessage(message);
            i++;
        }
        System.out.println("Sent 4 messages");

        CountDownLatch countDownLatch = new CountDownLatch(4);
        ServiceBusProcessorClient serviceBusProcessorClient = new ServiceBusClientBuilder()
                .connectionString(serviceBusConnectionString)
                .sessionProcessor()
                .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
                .queueName("test-session-sb")
                .processMessage(context -> {
                    System.out.println("Received message " + context.getMessage().getMessageId());
                    countDownLatch.countDown();
                })
                .processError(error -> System.out.println("Error while processing message " + error.getException().getMessage()))
                .buildProcessorClient();

        serviceBusProcessorClient.start();
        countDownLatch.await();
        System.out.println("Stopping processor");
        serviceBusProcessorClient.stop();
    }

}
