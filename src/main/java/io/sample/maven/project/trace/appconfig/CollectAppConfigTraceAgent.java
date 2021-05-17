package io.sample.maven.project.trace.appconfig;

import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;

public class CollectAppConfigTraceAgent {
    public static void main(String[] args) {

        Span test = GlobalOpenTelemetry.getTracer("test").spanBuilder("app-config-set").startSpan();

        ConfigurationClient client = new ConfigurationClientBuilder()
                .connectionString("") // TODO: set your connection string here
                .buildClient();
        final Scope scope = test.makeCurrent();
        try {
            // Thread bound (sync) calls will automatically pick up the parent span and you don't need to pass it explicitly.
            client.setConfigurationSetting("hello", "text", "World");
        } finally {
            test.end();
            scope.close();
        }

    }
}
