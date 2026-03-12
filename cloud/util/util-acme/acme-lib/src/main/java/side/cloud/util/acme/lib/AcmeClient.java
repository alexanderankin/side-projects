package side.cloud.util.acme.lib;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import side.cloud.util.acme.lib.model.AcmeResources.*;
import side.cloud.util.acme.lib.model.SupportedClientKeyPair;

import java.net.URI;

@Slf4j
@Data
@Accessors(chain = true)
public class AcmeClient {
    private final AcmeClientService acmeClientService;
    @Getter(AccessLevel.NONE)
    private volatile Directory acmeDirectory;

    AcmeClient(AcmeClientService acmeClientService) {
        this.acmeClientService = acmeClientService;
    }

    @SuppressWarnings("unused")
    public static AcmeClient create(Config config) {
        try (var acmeFactory = new AcmeFactory()) {
            return acmeFactory.acmeClient(config);
        }
    }

    @SneakyThrows
    public Directory acmeDirectory() {
        if (acmeDirectory != null) {
            return acmeDirectory;
        }
        synchronized (this) {
            if (acmeDirectory != null) {
                return acmeDirectory;
            }
            return acmeDirectory = acmeClientService.directory();
        }
    }

    public String newNonce() {
        return acmeClientService.newNonce(acmeDirectory());
    }

    public Account newAccount(NewAccount newAccount) {
        return acmeClientService.newAccount(acmeDirectory(), newNonce(), newAccount);
    }

    public Account fetchAccount(NewAccount newAccount) {
        newAccount.setOnlyReturnExisting(true);
        return newAccount(newAccount);
    }

    public void keyChange(Account account) {
        // but interesting
        throw new UnsupportedOperationException();
    }

    public void deactivateAccount(Account account) {
        throw new UnsupportedOperationException();
    }

    public Order newOrder(NewOrder newOrder) {
        return acmeClientService.newOrder(acmeDirectory(), newNonce(), newOrder);
    }

    @Data
    @Accessors(chain = true)
    public static class Config {
        @NotNull
        @Valid
        final SupportedClientKeyPair keyPair;
        @NotNull
        final URI directoryUrl;
        @NotBlank
        String userAgent;
    }
}
