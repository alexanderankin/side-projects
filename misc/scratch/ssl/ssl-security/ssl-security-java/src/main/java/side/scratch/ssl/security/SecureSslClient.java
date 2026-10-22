package side.scratch.ssl.security;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.HostnameVerificationPolicy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import javax.net.ssl.*;
import java.net.URI;
import java.security.KeyStore;
import java.security.cert.*;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class SecureSslClient {
    final DnsResolver dnsResolver;
    final RestClient restClient;

    @SneakyThrows
    public SecureSslClient(DnsResolver dnsResolver) {
        this.dnsResolver = dnsResolver;
        restClient = RestClient.builder()
            .requestFactory(new HttpComponentsClientHttpRequestFactory(
                HttpClients.custom()
                    .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                        .setTlsSocketStrategy(new DefaultClientTlsStrategy(
                            sslContext(),
                            HostnameVerificationPolicy.BUILTIN,
                            null
                        ))
                        .setDnsResolver(dnsResolver)
                        .build())
                    .build()
            ))
            .build();
    }

    int makeRequest(URI uri) {
        return makeRequest(uri, restClient);
    }

    int makeRequest(URI uri, RestClient restClient) {
        try {
            var response = restClient.get().uri(uri).retrieve().toBodilessEntity();
            return response.getStatusCode().value();
        } catch (ResourceAccessException e) {
            log.error("could not access resource {}", uri, e);
            throw e;
        } catch (RestClientResponseException e) {
            log.error("could not request {} ({}): {}", uri, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw e;
        }
    }

    @SneakyThrows
    SSLContext sslContext() {
        final boolean useSystemDefault = false;
        if (useSystemDefault) {
            return SSLContexts.createDefault();
        }

        TrustManagerFactory defaultTmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        defaultTmf.init((KeyStore) null);

        Set<TrustAnchor> anchors = new HashSet<>();
        for (TrustManager trustManager : defaultTmf.getTrustManagers()) {
            if (trustManager instanceof X509TrustManager tm) {
                for (X509Certificate cert : tm.getAcceptedIssuers()) {
                    anchors.add(new TrustAnchor(cert, null));
                }
            }
        }

        PKIXBuilderParameters pkix = new PKIXBuilderParameters(anchors, new X509CertSelector());
        pkix.setRevocationEnabled(false);

        PKIXRevocationChecker revocationChecker =
            (PKIXRevocationChecker) CertPathValidator.getInstance("PKIX").getRevocationChecker();
        revocationChecker.setOptions(Set.of(
            PKIXRevocationChecker.Option.PREFER_CRLS,
            PKIXRevocationChecker.Option.NO_FALLBACK
        ));
        pkix.addCertPathChecker(revocationChecker);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
        tmf.init(new CertPathTrustManagerParameters(pkix));

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);
        return sslContext;
    }
}
