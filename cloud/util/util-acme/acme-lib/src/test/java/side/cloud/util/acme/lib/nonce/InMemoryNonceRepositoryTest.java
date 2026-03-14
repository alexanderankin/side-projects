package side.cloud.util.acme.lib.nonce;

class InMemoryNonceRepositoryTest extends NonceRepositoryTestSuite {
    @Override
    protected NonceRepository createRepository() {
        return new InMemoryNonceRepository();
    }
}
