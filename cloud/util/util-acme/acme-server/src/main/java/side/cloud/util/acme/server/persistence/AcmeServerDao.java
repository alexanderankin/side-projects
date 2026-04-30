package side.cloud.util.acme.server.persistence;

import side.cloud.util.acme.lib.keys.SupportedClientKeyPair;
import side.cloud.util.acme.lib.model.AcmeResources;

import java.util.List;

public interface AcmeServerDao {
    ServerAccountEntity getAccountById(String id);
    ServerAccountEntity getAccountByKeyHash(String keyHash);

    ServerAccountEntity saveAccount(ServerAccountEntity serverAccountEntity);

    List<String> listOrdersForAccount(String accountId, String lastId);

    record ServerAccountEntity(String id, String keyHash, AcmeResources.Account account, SupportedClientKeyPair keyPair) {}
}
