package side.cloud.util.acme.lib.nonce;

import side.cloud.util.acme.lib.model.Repository;

import java.util.function.Function;

public interface NonceRepository extends Repository<String> {
    @Override
    default Function<String, String> getKeyFunction() {
        return Function.identity();
    }
}
