package side.cloud.util.acme.lib;

import side.cloud.util.acme.lib.model.AcmeResources.Directory;

public interface AcmeClientServiceI {
    String newNonce();

    Directory directory();


}
