import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

public class SecretsManagerService {
    public static final SecretsManagerService INSTANCE = new SecretsManagerService();

    @SneakyThrows
    AwsBasicCredentials fetchFromSsm() {
        try (var client = SecretsManagerClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(DefaultCredentialsProvider.builder()
                        // when working with this project, set the cred in this profile
                        // "in prod", will pull from environment
                        .profileName("dynamodb-examples")
                        .build())
                .build()) {

            var secretValue = client.getSecretValue(GetSecretValueRequest.builder()
                            .secretId("dynamodb-examples-dynamodb-user-creds")
                            .build())
                    .secretString();

            var secret = new ObjectMapper().readValue(secretValue, AwsBasicCredSecret.class);

            return AwsBasicCredentials.create(
                    secret.getKey(),
                    secret.getSecret()
            );
        }
    }

    @Data
    @Accessors(chain = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class AwsBasicCredSecret {
        @JsonProperty("aws_access_key_id")
        String key;
        @JsonProperty("aws_secret_access_key")
        String secret;
    }
}
