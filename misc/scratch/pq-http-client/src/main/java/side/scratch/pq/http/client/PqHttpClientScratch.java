package side.scratch.pq.http.client;

import io.netty.handler.ssl.OpenSslContextOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.server.HttpServer;

import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * program version of:
 * {@code openssl s_client -connect localhost:8443 -groups X25519Kyber768Draft00}
 * <p>
 */
@Slf4j
public class PqHttpClientScratch {

    @SneakyThrows
    public static void main(String[] args) {
        PemPair pemPair = PemPair.create();
        var disposableServer = httpServer(pemPair);

        log.info("started server: {}", disposableServer.address());

        var client = httpClient(pemPair);

        String body = client
                .get()
                .uri("https://localhost:8443/")
                .responseSingle((resp, content) -> content.asString())
                .block();

        log.info("response: {}", body);

        disposableServer.disposeNow();
        log.info("done");
    }

    @SneakyThrows
    private static HttpClient httpClient(PemPair pemPair) {
        // 1. TLS context: OpenSSL provider + TLS-1.3 + ONLY X25519Kyber768Draft00
        SslContext sslCtx = SslContextBuilder.forClient()
                // needs netty-tcnative-boringssl
                .sslProvider(SslProvider.OPENSSL)
                .protocols("TLSv1.3")
                // restrict the key-share list
                .option(OpenSslContextOption.GROUPS,
                        new String[]{"X25519Kyber768Draft00"})
                // Trust your self-signed server cert; swap for a real TrustManager in prod
                .trustManager(pemPair.tmf())
                .build();

        return HttpClient.create()
                .secure(spec -> spec.sslContext(sslCtx));
    }

    private static DisposableServer httpServer(PemPair pemPair) throws Exception {
        SslContext sslCtx = SslContextBuilder.forServer(pemPair.certStream(), pemPair.privateKeyStream(), null)
                .option(OpenSslContextOption.GROUPS, new String[]{"X25519Kyber768Draft00"})
                // use BoringSSL
                .sslProvider(io.netty.handler.ssl.SslProvider.OPENSSL)
                // no TLS 1.2 fallback
                .protocols("TLSv1.3")
                .build();

        // gpt line that doesn't work, needs investigating
        // ((OpenSslContext) sslCtx).setKeyShareGroups("X25519Kyber768Draft00");

        return HttpServer.create()
                .port(8443)
                .secure(spec -> spec.sslContext(sslCtx))
                .handle((request, response) ->
                        response
                                .header("Content-Type", "text/html; charset=UTF-8")
                                .sendString(Mono.just("<h1>It Worked</h1>\n"))
                )
                .bindNow();
    }

    public record PemPair(String cert, String privateKey) {
        public static PemPair create() throws Exception {
            var ssc = new SelfSignedCertificate();               // <── does all the heavy lifting
            return new PemPair(
                    Files.readString(ssc.certificate().toPath()),  // public cert PEM
                    Files.readString(ssc.privateKey().toPath())    // private key PEM
            );
        }

        public InputStream certStream() {
            return new ByteArrayInputStream(cert().getBytes(StandardCharsets.UTF_8));
        }

        public InputStream privateKeyStream() {
            return new ByteArrayInputStream(privateKey().getBytes(StandardCharsets.UTF_8));
        }

        @SneakyThrows
        public TrustManagerFactory tmf() {
            // Parse the cert from PEM
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            var cert = (X509Certificate) certFactory.generateCertificate(certStream());

            // Put the cert into an in-memory keystore
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null); // init empty
            trustStore.setCertificateEntry("server", cert);

            // Create a TrustManagerFactory from that keystore
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            return tmf;
        }
    }
}
