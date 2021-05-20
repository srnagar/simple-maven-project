package io.sample.maven.project.trace.servicebus;

import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.ServiceBusSessionReceiverClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;

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

        ServiceBusSessionReceiverClient serviceBusReceiverClient = new ServiceBusClientBuilder()
                .connectionString(serviceBusConnectionString)
                .sessionReceiver()
                .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
                .queueName("test-session-sb")
                .buildClient();
        IterableStream<ServiceBusReceivedMessage> serviceBusReceivedMessages = serviceBusReceiverClient
                .acceptSession("tennis")
                .receiveMessages(4);
        serviceBusReceivedMessages.forEach(message -> System.out.println("Received message " + message.getMessageId()));
        serviceBusSenderClient.close();
        serviceBusReceiverClient.close();
    }
}
