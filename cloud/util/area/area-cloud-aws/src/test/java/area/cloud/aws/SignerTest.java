package area.cloud.aws;

import area.cloud.aws.LearnDescribeVpcs.AwsApiSigner.AwsCredentials;
import area.cloud.aws.LearnDescribeVpcs.PojoClientRequest;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SignerTest {

    public static final DateTimeFormatter SDF = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    static List<Arguments> authorizationTestCases() {
        return List.of(
                Arguments.of(
                        new PojoClientRequest(),
                        new ServiceRequestDetails(Instant.parse("2013-05-24T00:00:00.000Z"), "s3", "us-east-1"),
                        new AwsCredentials().setAccessKeyId("AKIASIMSIM").setSecretAccessKey("SEZAM"),
                        """
                                AWS4-HMAC-SHA256\s
                                Credential=AKIASIMSIM/20130524/us-east-1/s3/aws4_request,
                                SignedHeaders=host;range;x-amz-date,
                                Signature=fe5f80f77d5fa3beca038a248ff027d0445342fe2855ddc963176630326f1024
                                """
                )
        );
    }

    PojoClientRequest prepAuthHeaders(PojoClientRequest clientRequest, ServiceRequestDetails service) {
        clientRequest.setHeaders(Objects.requireNonNullElseGet(clientRequest.getHeaders(), HttpHeaders::new));
        clientRequest.getHeaders().set("X-Amz-Date", SDF.format(service.date()));

        return clientRequest;
    }

    String authorization(HttpRequest httpRequest, AwsCredentials credentials, ServiceRequestDetails srd) {
        // DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").format(java.time.LocalDateTime.now(ZoneOffset.UTC)
        String amzDateTime = httpRequest.getHeaders().getFirst("X-Amz-Date");
        Objects.requireNonNull(amzDateTime, "cannot determine authorization without 'X-Amz-Date' header");

        var amzDate = LocalDateTime.parse(amzDateTime, SDF).toLocalDate().toString();

        List<String> signedHeaders = new ArrayList<>();
        String canonicalRequest = "";
        String toSign = "AWS4-HMAC-SHA256\n" + amzDateTime + "\n" + amzDate + "/" + srd.region() + "/" + srd.service() + "/aws4_request\n" + this.hashString(cRequest);
        ;

        var signature = new MacChain("AWS4" + credentials.secretAccessKey)
                .chain(amzDate)
                .chain(srd.region())
                .chain(srd.service())
                .chain("aws4_request")
                .hmac(toSign);
        
        // var signature = this.hmac(
        //         this.hmac(
        //                 this.hmac(
        //                         this.hmac(
        //                                 this.hmac("AWS4" + credentials.secretAccessKey, amzDate),
        //                                 srd.region()
        //                         ),
        //                         srd.service()
        //                 ),
        //                 "aws4_request"
        //         ),
        //         toSign);

        return "AWS4-HMAC-SHA256 Credential=" + credentials.getAccessKeyId() +
               "/" + amzDate + "/" + srd.region() + "/" + srd.service() + "/aws4_request" +
               ",SignedHeaders=" + String.join(":", signedHeaders) +
               ",Signature=" + signature;
    }

    String hmac(String key, String data) {
        return Hex.encodeHexString(HmacUtils.getInitializedMac(HmacAlgorithms.HMAC_SHA_256, key.getBytes(StandardCharsets.UTF_8)).doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    @ParameterizedTest
    @MethodSource("authorizationTestCases")
    void test_authorization(PojoClientRequest httpRequest, ServiceRequestDetails service, AwsCredentials credentials, String expected) {
        assertEquals(expected, authorization(prepAuthHeaders(httpRequest, service), credentials, service));
    }

    @Test
    void test() {
        // System.out.println(Hex.encodeHexString(DigestUtils.sha256("abc")));
        // good!

        System.out.println(hmac("key", "data"));
    }


    record ServiceRequestDetails(Instant date, String service, String region) {
    }
    
    record MacChain(String key) {
        MacChain chain(String data) {
            return new MacChain(hmac(data));
        }

        String hmac(String data) {
            return Hex.encodeHexString(HmacUtils.getInitializedMac(HmacAlgorithms.HMAC_SHA_256, key.getBytes(StandardCharsets.UTF_8)).doFinal(data.getBytes(StandardCharsets.UTF_8)));
        }
    }
}
