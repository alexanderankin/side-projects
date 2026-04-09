package side.cloud.util.acme.lib.challenges;

import org.jspecify.annotations.NonNull;

import java.util.Map;

public interface Presenter {
    default void add(Type type, String key, String value) {
        switch (type) {
            case http -> addHttp01(key, value);
            case dns -> addDnsTxt(key, value);
            case tls -> addTlsAlpn01(key, value);
        }
    }

    default void delete(Type type, String key) {
        switch (type) {
            case http -> deleteHttp01(key);
            case dns -> deleteDnsTxt(key);
            case tls -> deleteTlsAlpn01(key);
        }
    }

    void addHttp01(String token, String content);

    void deleteHttp01(String token);

    void addDnsTxt(String host, String value);

    void deleteDnsTxt(String host);

    void addTlsAlpn01(String host, String keyAuthorization);

    void deleteTlsAlpn01(String host);

    enum Type {
        http, dns, tls;

        private static final Map<String, Type> TYPES = Map.of("http", http, "dns", dns, "tls", tls);

        public static Type valueOfOrNull(String type) {
            return TYPES.get(type);
        }

        public static Type valueOfAcmeNameOrNull(@NonNull String acmeName) {
            return valueOfOrNull(acmeName.substring(0, acmeName.indexOf('-')));
        }
    }
}
