package side.cloud.util.acme.lib;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import side.cloud.util.acme.lib.model.SupportedClientKeyPairAlgorithm;

public class AcmeFactory implements AutoCloseable {
    ValidatorFactory factory;
    Validator validator;

    public AcmeFactory() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    static void main() {
        System.out.println(
                SupportedClientKeyPairAlgorithm.EdDSA.serialize(
                        SupportedClientKeyPairAlgorithm.EdDSA.generate()
                )
        );
    }

    private void validate(Object o, Class<?>... groups) {
        var violations = validator.validate(o, groups);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    public AcmeClient acmeClient(AcmeClient.Config config) {
        validate(config);
        return new AcmeClient(new AcmeClientService(new AcmeClientService.Config(config.directoryUrl)
                .setUserAgent(config.userAgent)));
    }

    @Override
    public void close() {
        factory.close();
    }

}
