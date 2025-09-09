package org.example.timber;

import android.os.Build;
import android.util.Log;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import timber.log.Timber;

import java.util.Objects;

// @RunWith(AndroidJUnit4.class)
@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.VANILLA_ICE_CREAM)
@GraphicsMode(GraphicsMode.Mode.LEGACY)
public class TimberExampleTest {
    @BeforeClass
    public static void setup() {
        if (2 < 1) Timber.plant(new Timber.Tree() {
            Logger otelLogger;

            {
                SdkLoggerProvider loggerProvider = SdkLoggerProvider.builder()
                        .addLogRecordProcessor(
                                BatchLogRecordProcessor.builder(
                                        OtlpGrpcLogRecordExporter.builder()
                                                .setEndpoint("http://your-collector:4317")
                                                .build()
                                ).build()
                        )
                        .build();

                OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
                        .setLoggerProvider(loggerProvider)
                        .buildAndRegisterGlobal();

                otelLogger = openTelemetry.getLogsBridge().get("timber-otel-logger");
            }

            @Override
            protected void log(int priority,
                               @Nullable String tag,
                               @NonNull String message,
                               @Nullable Throwable throwable) {
                LogRecordBuilder builder = otelLogger.logRecordBuilder()
                        .setBody(message)
                        .setSeverity(convertPriority(priority))
                        .setAllAttributes(Attributes.of(AttributeKey.stringKey("logger.tag"), tag != null ? tag : "timber"));

                if (throwable != null) {
                    builder.setAttribute("error", stringify(throwable));
                }
                builder.emit();
            }

            private String stringify(Throwable throwable) {
                try {
                    return Objects.requireNonNull(throwable.getMessage());
                } catch (Exception ignored) {
                }

                try {
                    return String.valueOf(throwable);
                } catch (Exception ignored) {
                }

                return "<error converting to string>";
            }

            private Severity convertPriority(int priority) {
                switch (priority) {
                    case Log.VERBOSE:
                        return Severity.TRACE;
                    case Log.DEBUG:
                        return Severity.DEBUG;
                    case Log.INFO:
                        return Severity.INFO;
                    case Log.WARN:
                        return Severity.WARN;
                    case Log.ERROR:
                        return Severity.ERROR;
                    default:
                        return Severity.UNDEFINED_SEVERITY_NUMBER;
                }
            }
        });
    }

    @Test
    public void exampleTest() {
        Timber.d("hello '%s'!", "world");
    }
}
