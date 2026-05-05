package side.cloud.util.acme.server.persistence;

import lombok.Data;
import lombok.experimental.Accessors;
import side.cloud.util.acme.lib.keys.SupportedClientKeyPair;
import side.cloud.util.acme.lib.model.AcmeResources;

import java.util.Base64;
import java.util.List;
import java.util.Map;

public interface AcmeServerDao {
    ServerAccountEntity getAccountById(String id);

    ServerAccountEntity getAccountByKeyHash(String keyHash);

    ServerExternalAccountEntity getExternalAccountById(String id);

    void saveAccount(ServerAccountEntity serverAccountEntity);

    List<String> listOrdersForAccount(String accountId, int pageSize, String lastId);

    void saveOrder(ServerOrderEntity orderEntity);

    ServerOrderEntity getOrderById(String orderId);

    @Data
    @Accessors(chain = true)
    class ServerAccountEntity {
        String id;
        String keyHash;
        AcmeResources.Account account;
        SupportedClientKeyPair keyPair;
    }

    @Data
    @Accessors(chain = true)
    class ServerExternalAccountEntity {
        String id;
        String macBase64;

        public byte[] decodeMac() {
            return macBase64 == null ? null : Base64.getDecoder().decode(macBase64);
        }
    }

    @Data
    @Accessors(chain = true)
    class ServerOrderEntity {
        String id;
        String accountId;
        AcmeResources.Order order;
        List<String> authorizationIds;
        Map<String, AcmeResources.Authorization> authorizations;
        Map<String, List<String>> authorizationChallengeIds;
    }
}
