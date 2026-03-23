package side.cloud.util.acme.lib;

import lombok.RequiredArgsConstructor;
import side.cloud.util.acme.lib.model.AcmeResources.Account;
import side.cloud.util.acme.lib.model.AcmeResources.ResourceWithId;
import side.cloud.util.acme.lib.nonce.NonceService;

@RequiredArgsConstructor
public class AcmeServerService {
    private final NonceService nonceService;

    public String newNonce() {
        return nonceService.newNonce();
    }

    public ResourceWithId<Account> newAccount() {
        return null;
    }
}
