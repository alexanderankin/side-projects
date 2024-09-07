package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

class EmulatorS3ClientTest {

    @Test
    void test() {
        EmulatorS3Client emulatorS3Client = new EmulatorS3Client(new EmulatorS3Client.Config(), RestClient.builder());

//        emulatorS3Client.list
    }

    @Test
    void test_X_AmzDate() {

        Instant now = Instant.now();
        LocalDateTime l = LocalDateTime.ofInstant(now, ZoneOffset.UTC);

        String format = EmulatorS3Client.DTF.format(l);

        System.out.println(format);
    }
}
