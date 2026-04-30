package side.cloud.util.acme.server.persistence;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryAcmeServerDao implements AcmeServerDao {
    final Map<String, ServerAccountEntity> accountByKeyHash = new ConcurrentHashMap<>();
    final Map<String, ServerAccountEntity> accountById = new ConcurrentHashMap<>();
    final Map<String, List<String>> ordersByAccountId = new ConcurrentHashMap<>();
    final Map<String, ServerOrderEntity> orderById = new ConcurrentHashMap<>();

    @Override
    public ServerAccountEntity getAccountById(String id) {
        return accountById.get(id);
    }

    @Override
    public ServerAccountEntity getAccountByKeyHash(String keyHash) {
        return accountByKeyHash.get(keyHash);
    }

    @Override
    public void saveAccount(ServerAccountEntity serverAccountEntity) {
        accountByKeyHash.put(serverAccountEntity.getKeyHash(), serverAccountEntity);
        accountById.put(serverAccountEntity.getId(), serverAccountEntity);
    }

    @Override
    public List<String> listOrdersForAccount(String accountId, String lastId) {
        return ordersByAccountId.get(accountId);
    }

    @Override
    public void saveOrder(ServerOrderEntity orderEntity) {
        orderById.put(orderEntity.getId(), orderEntity);
    }
}
