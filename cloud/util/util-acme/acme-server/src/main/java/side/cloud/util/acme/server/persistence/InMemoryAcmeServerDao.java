package side.cloud.util.acme.server.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

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
        ordersByAccountId.put(serverAccountEntity.getId(), new ArrayList<>());
    }

    @Override
    public List<String> listOrdersForAccount(String accountId, int pageSize, String lastId) {
        if (lastId == null) {
            return ordersByAccountId.get(accountId).stream().limit(pageSize).toList();
        }
        return ordersByAccountId.get(accountId).stream()
                .dropWhile(Predicate.not(Predicate.isEqual(lastId))) // drop before it
                .dropWhile(Predicate.isEqual(lastId)) // drop it too
                .limit(pageSize)
                .toList();
    }

    @Override
    public void saveOrder(ServerOrderEntity orderEntity) {
        orderById.put(orderEntity.getId(), orderEntity);
        ordersByAccountId.get(orderEntity.getAccountId()).add(orderEntity.getId());
    }

    @Override
    public ServerOrderEntity getOrderById(String orderId) {
        return orderById.get(orderId);
    }
}
