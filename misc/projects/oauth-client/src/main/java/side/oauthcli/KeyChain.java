package side.oauthcli;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@SuppressWarnings("unused")
public interface KeyChain {
    static KeyChain forCurrentOs() {
        return forOs(Os.current());
    }

    static KeyChain forOs(Os os) {
        return switch (os) {
            case mac -> new MacKeyChain();
            case null, default -> throw new UnsupportedOperationException("unsupported os: " + os);
        };
    }

    String getNamespace();
    KeyChain setNamespace(String namespace);

    boolean exists(String key);

    String get(String key);

    List<String> list();

    void set(String key, String value);

    void update(String key, String value);

    void delete(String key);

    enum Os {
        win, lin, mac;

        static Os current() {
            return Os.valueOf(System.getProperty("os.name").toLowerCase().substring(0, 3));
        }
    }

    @Data
    @Accessors(chain = true)
    class MacKeyChain implements KeyChain {
        public static final String NAMESPACE = "info.ankin.side-projects.oauth-cli";

        private String namespace = NAMESPACE;

        @Override
        public boolean exists(String key) {
            return KeyChainMacOsSupport.readText(namespace, key).isPresent();
        }

        @Override
        public String get(String key) {
            return KeyChainMacOsSupport.readText(namespace, key).orElseThrow();
        }

        @Override
        public List<String> list() {
            return KeyChainMacOsSupport.list(namespace);
        }

        @Override
        public void set(String key, String value) {
            KeyChainMacOsSupport.saveText(namespace, key, value);
        }

        @Override
        public void update(String key, String value) {
            KeyChainMacOsSupport.updateText(namespace, key, value);
        }

        @Override
        public void delete(String key) {
            if (!KeyChainMacOsSupport.deleteText(namespace, key)) {
                throw new IllegalArgumentException("key does not exist: " + key);
            }
        }
    }
}
