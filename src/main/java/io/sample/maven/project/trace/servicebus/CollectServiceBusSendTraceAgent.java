package io.sample.maven.project.trace.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;

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
        String[] sessions = {"soccer", "cricket", "tennis", "badminton"};
        ServiceBusSenderClient serviceBusSenderClient = new ServiceBusClientBuilder()
                .connectionString("")
                .sender()
                .buildClient();

        int i = 0;
        while (i < 4) {
            i++;
            ServiceBusMessage message = new ServiceBusMessage("hello " + i)
                    .setMessageId(String.valueOf(i))
                    .setSessionId(sessions[i % 4]);
            serviceBusSenderClient.sendMessage(message);
        }
    }
}
