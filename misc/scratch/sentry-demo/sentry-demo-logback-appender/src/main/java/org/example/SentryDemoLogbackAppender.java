package org.example;

import ch.qos.logback.classic.LoggerContext;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import io.sentry.SentryOptions;
import io.sentry.logback.SentryAppender;
import io.sentry.logger.LoggerApi;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;

@Slf4j
@SpringBootApplication
class SentryDemoLogbackAppender {
    public static void main(String[] args) {
        System.setProperty("sentry.enabled", "true");
        System.setProperty("spring.config.import", "optional:" + Path.of(System.getProperty("user.home"), ".sentry-token.yaml").toString());
        System.setProperty("logging.level.org.example", "DEBUG");

        SpringApplication.withHook(
                ignored -> new SpringApplicationRunListener() {
                    @Override
                    public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext, ConfigurableEnvironment environment) {
                        setupSentry(bootstrapContext, environment);
                    }
                },
                () -> {
                    // default main method body - if extract sentry to library with service files and configs to application.yaml
                    return SpringApplication.run(SentryDemoLogbackAppender.class, args);
                }
        );
    }

    static void setupSentry(ConfigurableBootstrapContext ignored, ConfigurableEnvironment environment) {
        if (!(LoggerFactory.getILoggerFactory() instanceof LoggerContext loggerContext)) {
            throw new UnsupportedOperationException();
        }

        var sentryProps = Binder.get(environment).bindOrCreate(SentryProps.PREFIX, SentryProps.class);

        if (!sentryProps.isEnabled()) {
            log.warn("sentry is not enabled");
            return;
        }

        log.debug("using sentryProps: {}", sentryProps);

        var sentryAppender = new SentryAppender();
        SentryOptions options = new SentryOptions();
        options.setDsn(sentryProps.getDsn());
        options.setEnabled(sentryProps.isEnabled());
        options.getLogs().setEnabled(true);
        sentryAppender.setOptions(options);
        sentryAppender.setContext(loggerContext);
        sentryAppender.start();

        loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(sentryAppender);
    }

    @Bean
    ApplicationRunner init() {
        return args -> {
            log.debug("Hello World without sdk debug - on debug level!");
            log.info("Hello World without sdk debug - on info level!");
            log.warn("Hello World without sdk debug - on warn level!");

            // almost thought I had to add this lol:
            // Sentry.getGlobalScope().getClient().flush(10_000);
        };
    }

    @Data
    @Accessors(chain = true)
    @Component
    @ConfigurationProperties(prefix = SentryProps.PREFIX)
    @Validated
    static class SentryProps {
        public static final String PREFIX = "sentry";

        /**
         * should sentry be enabled
         */
        boolean enabled;

        String projectName;
        String dsn;

        @AssertTrue
        boolean dsnPresentIfEnabled() {
            return !enabled || StringUtils.hasText(dsn);
        }
    }
}
