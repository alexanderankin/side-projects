package org.example;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@Slf4j
class ConventionsTest {
    @Test
    void test_nativeAccess() {
        boolean isNativeEnabled = getClass().getModule().isNativeAccessEnabled();
        log.info("Is native access allowed? {}", isNativeEnabled);
        assertThat(isNativeEnabled, is(true));
    }

    @Test
    void test() {
        List<String> jvmArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
        log.info("JVM arguments: {}", jvmArgs);
        assertThat(
            jvmArgs.stream()
                .filter(s -> s.startsWith("-javaagent:") &&
                    StreamSupport.stream(Path.of(s.substring("-javaagent:".length())).spliterator(), false).map(Path::toString).toList()
                        .contains("mockito-core"))
                .findAny().orElse(null),
            is(notNullValue()));
    }
}
