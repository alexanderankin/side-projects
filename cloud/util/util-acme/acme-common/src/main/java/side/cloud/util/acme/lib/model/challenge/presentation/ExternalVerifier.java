package side.cloud.util.acme.lib.model.challenge.presentation;

import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.web.client.RestClient;
import org.xbill.DNS.*;
import org.xbill.DNS.Record;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public class ExternalVerifier {
    /**
     * e.g. 8.8.8.8
     */
    private final String dnsResolver;
    private final int dnsResolverPort;
    private final RestClient restClient;

    public static ExternalVerifier noOp() {
        return new ExternalVerifier(null, -1, null) {
            @Override
            public boolean verifyDns(String fqdn, String value) {
                return true;
            }

            @Override
            public boolean verifyHttp(URI uri, String value) {
                return true;
            }
        };
    }

    @SneakyThrows
    List<String> queryTxt(String fqdn) {
        Lookup lookup = new Lookup(fqdn, Type.TXT);
        lookup.setResolver(new SimpleResolver(InetSocketAddress.createUnresolved(dnsResolver, dnsResolverPort)));

        Record[] records = lookup.run();
        if (records == null) return List.of();

        List<String> result = new ArrayList<>();
        for (Record r : records) {
            TXTRecord txt = (TXTRecord) r;
            result.add(String.join("", txt.getStrings()));
        }
        return result;
    }

    public boolean verifyDns(String fqdn, String value) {
        return queryTxt(fqdn).contains(value);
    }

    public boolean verifyHttp(URI uri, String value) {
        return Objects.equals(restClient.get().uri(uri).retrieve().body(String.class), value);
    }
}
