package side.cloud.util.acme.lib.nonce;

public interface NonceService {
    String newNonce();

    boolean nonceValid(String nonce);

    boolean useNonce(String nonce);

    void startCleanup();
}
