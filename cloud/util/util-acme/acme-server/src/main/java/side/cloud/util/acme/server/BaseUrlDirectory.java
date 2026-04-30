package side.cloud.util.acme.server;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.springframework.web.util.UriComponentsBuilder;
import side.cloud.util.acme.lib.model.AcmeResources.Directory;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Builder
@Data
@Accessors(chain = true)
public class BaseUrlDirectory {
    private static final List<Map.Entry<Function<BaseUrlDirectory, String>, BiConsumer<Directory, URI>>> MAPPINGS =
            List.of(
                    Map.entry(BaseUrlDirectory::getKeyChange, Directory::setKeyChange),
                    Map.entry(BaseUrlDirectory::getNewAccount, Directory::setNewAccount),
                    Map.entry(BaseUrlDirectory::getNewAuthz, Directory::setNewAuthz),
                    Map.entry(BaseUrlDirectory::getNewNonce, Directory::setNewNonce),
                    Map.entry(BaseUrlDirectory::getNewOrder, Directory::setNewOrder),
                    Map.entry(BaseUrlDirectory::getRenewalInfo, Directory::setRenewalInfo),
                    Map.entry(BaseUrlDirectory::getRevokeCert, Directory::setRevokeCert),
                    Map.entry(BaseUrlDirectory::getMetaTermsOfService,
                            (d, u) -> d.setMeta(
                                            Objects.requireNonNullElseGet(
                                                    d.getMeta(),
                                                    Directory.Meta::new
                                            )
                                    )
                                    .getMeta()
                                    .setTermsOfService(u))
            );

    @NonNull
    final URI baseUrl;
    String keyChange;
    String newAccount;
    String newAuthz;
    String newNonce;
    String newOrder;
    String renewalInfo;
    String revokeCert;
    String metaTermsOfService;

    public Directory baseUrlDirectory() {
        var urlBuilder = UriComponentsBuilder.fromUri(baseUrl);
        var directory = new Directory();

        for (var mapping : MAPPINGS) {
            var reader = mapping.getKey();
            var writer = mapping.getValue();

            var value = reader.apply(this);
            if (value == null)
                continue;
            var url = urlBuilder.replacePath(value).build().toUri();
            writer.accept(directory, url);
        }

        return directory;
    }
}
