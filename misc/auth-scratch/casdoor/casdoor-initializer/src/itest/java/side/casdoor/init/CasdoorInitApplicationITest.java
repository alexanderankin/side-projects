package side.casdoor.init;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.GenericContainer;
import side.casdoor.init.CasdoorInitApplication.Init;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@Import(CasdoorInitApplicationTestApp.TestcontainersConfig.class)
public class CasdoorInitApplicationITest {
    static void fixLogging() {
        ((LoggerContext) LoggerFactory.getILoggerFactory())
                .getLogger(Logger.ROOT_LOGGER_NAME)
                .iteratorForAppenders()
                .forEachRemaining(a -> {
                    if (a instanceof ConsoleAppender<ILoggingEvent> consoleAppender) {
                        consoleAppender.setOutputStream(System.err);
                    }
                });
    }

    @Autowired
    GenericContainer<?> casdoorContainer;

    @SneakyThrows
    @Test
    void test() {
        fixLogging();
        Init casdoorInit = new Init();
        casdoorInit.casdoorUrl = casdoorContainer.getEnvMap().get("casdoorUrl");

        String output;
        try (var tempStdOut = new TempStdOut()) {
            casdoorInit.run();
            output = tempStdOut.out.toString(StandardCharsets.UTF_8);
        }

        // {"application":"sample-client","organization":"built-in","cert":"","client_id":"sample-client-id","client_secret":"sample-client-secret"}
        Output outputDto = new ObjectMapper().readValue(output, Output.class);
        assertThat(outputDto.getApplication(), is(not(blankOrNullString())));
        assertThat(outputDto.getOrganization(), is(not(blankOrNullString())));
        assertThat(outputDto.getCert(), is(not(blankOrNullString())));
        assertThat(outputDto.getCert(), containsString("-----BEGIN CERTIFICATE-----"));
        assertThat(outputDto.getClientId(), is(not(blankOrNullString())));
        assertThat(outputDto.getClientSecret(), is(not(blankOrNullString())));
    }

    @Data
    @Accessors(chain = true)
    static class Output {
        String application;
        String organization;
        String cert;
        @JsonProperty("client_id")
        String clientId;
        @JsonProperty("client_secret")
        String clientSecret;
    }

    static class TempStdOut implements AutoCloseable {
        PrintStream old;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        TempStdOut() {
            old = System.out;
            System.setOut(new PrintStream(out));
        }

        @Override
        public void close() {
            System.setOut(old);
        }
    }
}
