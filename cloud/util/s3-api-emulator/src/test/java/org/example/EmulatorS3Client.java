package org.example;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestInitializer;
import org.springframework.lang.NonNull;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class EmulatorS3Client {
    static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
    static final DateTimeFormatter DTF_YMD = DateTimeFormatter.ofPattern("yyyyMMdd");
    final Config config;
    final RestClient.Builder builder;
    private RestClient restClient;

    public void init() {
        restClient = builder
                .baseUrl(config.getBaseUrl())
                .requestInitializer(new ClientHttpRequestInitializer() {

                    public static final String AUTH_SCHEME = "AWS4-HMAC-SHA256";

                    byte[] hmacSha256(String base, String input) {
                        return HmacUtils.getInitializedMac(HmacAlgorithms.HMAC_SHA_256, base.getBytes()).doFinal(input.getBytes());
                    }
                    byte[] hmacSha256(byte[] base, String input) {
                        return HmacUtils.getInitializedMac(HmacAlgorithms.HMAC_SHA_256, base).doFinal(input.getBytes());
                    }

                    @Override
                    public void initialize(@NonNull ClientHttpRequest request) {
                        var now = Instant.now();
                        var nowLdt = LocalDateTime.ofInstant(now, ZoneOffset.UTC);
                        String xAmzDate = DTF.format(nowLdt);
                        String ymdDate = DTF_YMD.format(nowLdt);

                        HttpHeaders ogHeaders = request.getHeaders();
                        HttpHeaders headers = ogHeaders;
                        String canonicalRequest = request.getMethod().name() + "\n" +
                                request.getURI().getPath() + "\n" +
                                UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams().entrySet().stream()
                                        .sorted(Map.Entry.comparingByKey())
                                        .flatMap(e -> e.getValue().stream().map(ee -> Map.entry(e.getKey(), ee)))
                                        .map(e -> e.getKey() + "=" + e.getValue())
                                        .collect(Collectors.joining("&")) + "\n" +
                                headers.entrySet().stream()
                                        .sorted(Map.Entry.comparingByKey())
                                        .flatMap(e -> e.getValue().stream().map(ee -> Map.entry(e.getKey(), ee)))
                                        .map(e -> e.getKey().toLowerCase() + ":" + e.getValue().strip())
                                        .collect(Collectors.joining("\n")) + "\n" + "\n" +
                                headers.keySet().stream().sorted().collect(Collectors.joining(";"));

                        String stringToSign = AUTH_SCHEME + "\n" +
                                xAmzDate + "\n" +
                                ymdDate + "/" + config.getRegion() + "/s3/aws4_request\n" +
                                DigestUtils.sha256Hex(canonicalRequest.getBytes(StandardCharsets.UTF_8));

                        var dateKey = hmacSha256("AWS4" + config.getSecretAccessKey(), ymdDate);
                        var dateRegionKey = hmacSha256(dateKey, config.getRegion());
                        var dateRegionServiceKey = hmacSha256(dateRegionKey, "s3");
                        var signingKey = hmacSha256(dateRegionServiceKey, "aws4_request");

                        String signature = HexFormat.of().formatHex(hmacSha256(signingKey, stringToSign));

                        headers.add("X-Amz-Date", xAmzDate);
                        headers.add("X-Amz-Content-SHA256", "20240902T152732Z");

                        String credential = String.join(
                                ", ",
                                "Credential=" + String.join("/", config.getAccessKeyId(), ymdDate, config.getRegion(), "s3", "aws4_request"),
                                "SignedHeaders=host;x-amz-content-sha256;x-amz-date",
                                "Signature=" + signature
                        );

                        headers.add(HttpHeaders.AUTHORIZATION, AUTH_SCHEME + " " + credential);
                    }
                })
                .build();
    }

    void list() {
        restClient.get();
    }

    @Data
    @Accessors(chain = true)
    public static class Config {
        String baseUrl;

        String region = "us-east-1";
        String accessKeyId;
        String secretAccessKey;
    }
}
