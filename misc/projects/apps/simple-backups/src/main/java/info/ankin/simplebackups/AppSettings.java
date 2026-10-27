package info.ankin.simplebackups;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public final class AppSettings {
    private final AwsAuthMethod awsAuthMethod;
    private final String awsProfile;
    private final String awsAccessKeyId;
    @ToString.Exclude
    private final String awsSecretAccessKey;
    private final String awsRegion;
    private final String sshUsername;
    private final String sshPrivateKey;
    private final BackupMethod preferredMethod;

    public enum AwsAuthMethod { profile, basic }

    public AppSettings(AwsAuthMethod awsAuthMethod, String awsProfile, String awsAccessKeyId,
                       String awsSecretAccessKey, String awsRegion, String sshUsername,
                       String sshPrivateKey, BackupMethod preferredMethod) {
        this.awsAuthMethod = awsAuthMethod == null ? AwsAuthMethod.profile : awsAuthMethod;
        this.awsProfile = fallback(awsProfile, "default");
        this.awsAccessKeyId = fallback(awsAccessKeyId, "");
        this.awsSecretAccessKey = fallback(awsSecretAccessKey, "");
        this.awsRegion = fallback(awsRegion, "us-east-1");
        this.sshUsername = fallback(sshUsername, System.getProperty("user.name"));
        this.sshPrivateKey = fallback(sshPrivateKey, "~/.ssh/id_ed25519");
        this.preferredMethod = preferredMethod == null ? BackupMethod.tar : preferredMethod;
    }

    public static AppSettings defaults() {
        return new AppSettings(null, null, null, null, null, null, null, null);
    }

    private static String fallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
