package side.cloud.util.acme.server.persistence;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryAcmeServerDao implements AcmeServerDao {
    final Map<String, ServerAccountEntity> accountByKeyHash = new ConcurrentHashMap<>();
    final Map<String, ServerAccountEntity> accountById = new ConcurrentHashMap<>();
    final Map<String, List<String>> ordersByAccountId = new ConcurrentHashMap<>();

    @Override
    public ServerAccountEntity getAccountById(String id) {
        return accountById.get(id);
    }

    @Override
    public ServerAccountEntity getAccountByKeyHash(String keyHash) {
        return accountByKeyHash.get(keyHash);
    }

    @Override
    public ServerAccountEntity saveAccount(ServerAccountEntity serverAccountEntity) {
        accountByKeyHash.put(serverAccountEntity.keyHash(), serverAccountEntity);
        accountById.put(serverAccountEntity.id(), serverAccountEntity);
        return serverAccountEntity;
    }

    @Override
    public List<String> listOrdersForAccount(String accountId, String lastId) {
        return ordersByAccountId.get(accountId);
    }
}
