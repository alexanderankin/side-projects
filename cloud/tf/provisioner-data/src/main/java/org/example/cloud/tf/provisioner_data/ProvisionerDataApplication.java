package org.example.cloud.tf.provisioner_data;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.filter.OncePerRequestFilter;

@SpringBootApplication
class ProvisionerDataApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProvisionerDataApplication.class, args);
    }

    @Configuration
    static class Config {
        @Bean
        CommonsRequestLoggingFilter requestLoggingFilter() {
            var loggingFilter = new CommonsRequestLoggingFilter();
            loggingFilter.setIncludeClientInfo(true);
            loggingFilter.setIncludeQueryString(true);
            loggingFilter.setIncludePayload(true);
            loggingFilter.setMaxPayloadLength(64000);
            loggingFilter.setIncludeHeaders(true);
            return loggingFilter;
        }

        @Bean
        @Order(Ordered.HIGHEST_PRECEDENCE)
        OncePerRequestFilter readerForRequestLoggingFilter() {
            return new DelegatingOncePerRequestFilter(
                    (request, response, filterChain) -> {
                        request = new org.springframework.web.util.ContentCachingRequestWrapper(request);
                        request.getInputStream().readAllBytes();
                        filterChain.doFilter(request, response);
                    }
            );
        }

        interface Filter {
            void filter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws Exception;

        }

        @RequiredArgsConstructor
        public static
        class DelegatingOncePerRequestFilter extends OncePerRequestFilter {
            final Filter filter;

            @SneakyThrows
            @Override
            protected void doFilterInternal(
                    @NonNull HttpServletRequest request,
                    @NonNull HttpServletResponse response,
                    @NonNull FilterChain filterChain
            ) {
                filter.filter(request, response, filterChain);
            }
        }
    }

    @RestController
    static class Ctrl {
        @PostMapping
        String mapping() {
            return "";
        }
    }
}
