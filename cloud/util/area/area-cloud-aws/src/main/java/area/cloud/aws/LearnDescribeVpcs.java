package area.cloud.aws;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.codec.digest.HmacAlgorithms.HMAC_SHA_256;

public class LearnDescribeVpcs {
    public static void main(String[] args) {
        var example = """
                https://ec2.amazonaws.com/?Action=DescribeVpcs
                &VpcId.1=vpc-081ec835f3EXAMPLE
                &vpcId.2=vpc-0ee975135dEXAMPLE
                &VpcId.3=vpc-06e4ab6c6cEXAMPLE
                &AUTHPARAMS
                """;

        RestClient restClient = RestClient.builder()
                .baseUrl("https://ec2.amazonaws.com/")
                .requestInterceptor(new AwsApiSigner())
                .build();

        ResponseEntity<String> entity = restClient.get()
                .uri(u -> u
                        .host("localhost")
                        .queryParam("Action", "DescribeVpcs")
                        .queryParam("VpcId.1", "vpc-080593ba48e14143f")
                        .build())
                .retrieve()
                .toEntity(String.class);

        System.out.println(entity);
    }

    @Data
    @Accessors(chain = true)
    static class PojoClientRequest implements HttpRequest {
        HttpMethod method;
        URI URI;
        HttpHeaders headers;
    }

    static class AwsApiSigner implements ClientHttpRequestInterceptor {
        String region;
        String service;
        AwsCredentials awsCredentials;

        @SneakyThrows
        @Override
        @NonNull
        public ClientHttpResponse intercept(@NonNull HttpRequest request,
                                            @NonNull byte[] body,
                                            @NonNull ClientHttpRequestExecution execution) {

            // https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_aws-signing.html
            // https://docs.aws.amazon.com/AmazonS3/latest/API/sig-v4-authenticating-requests.html
            var stringToSign = stringToSign(request, body);
            var signingKey = signingKey();


            var builder = UriComponentsBuilder.fromUri(request.getURI());
            // builder.queryParam("token", token);

            request = new PojoClientRequest()
                    .setMethod(request.getMethod())
                    // .setURI(request.getURI())
                    .setURI(builder.build().toUri())
                    .setHeaders(request.getHeaders())
            //
            ;

            return execution.execute(request, body);
        }

        String stringToSign(HttpRequest request, byte[] body) {
            // https://docs.aws.amazon.com/IAM/latest/UserGuide/create-signed-request.html


            throw new UnsupportedOperationException();
        }

        String signingKey() {
            String dateKey = hmac256("AWS4" + awsCredentials.getSecretAccessKey(), String.valueOf(LocalDate.now()));
            String dateRegionKey = hmac256(dateKey, region);
            String dateRegionServiceKey = hmac256(dateRegionKey, service);
            return hmac256(dateRegionServiceKey, "aws4_request");
        }

        String hmac256(String p1, String p2) {
            return new String(HmacUtils.getInitializedMac(HMAC_SHA_256, p1.getBytes(UTF_8)).doFinal(p2.getBytes(UTF_8)), UTF_8);
        }

        @Data
        @Accessors(chain = true)
        static class AwsCredentials {
            String accessKeyId;
            String secretAccessKey;
        }
    }
}
