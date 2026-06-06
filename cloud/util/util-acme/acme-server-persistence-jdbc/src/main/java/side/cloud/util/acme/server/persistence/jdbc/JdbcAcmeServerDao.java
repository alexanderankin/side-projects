package side.cloud.util.acme.server.persistence.jdbc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import side.cloud.util.acme.server.persistence.AcmeServerDao;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JdbcAcmeServerDao implements AcmeServerDao {
    private final JdbcClient jdbcClient;

    @Override
    public ServerAccountEntity getAccountById(String id) {
        return null;
    }

    @Override
    public ServerAccountEntity getAccountByKeyHash(String keyHash) {
        return null;
    }

    @Override
    public ServerExternalAccountEntity getExternalAccountById(String id) {
        return null;
    }

    @Override
    public void saveAccount(ServerAccountEntity serverAccountEntity) {

    }

    @Override
    public List<String> listOrdersForAccount(String accountId, int pageSize, String lastId) {
        return List.of();
    }

    @Override
    public void saveOrder(ServerOrderEntity orderEntity) {

    }

    @Override
    public ServerOrderEntity getOrderById(String orderId) {
        return null;
    }
}
