package info.ankin.simplebackups;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class S3SnapshotStore implements SnapshotStore {
    private static final int PART_SIZE = 16 * 1024 * 1024;
    private final S3Client client;
    private final AwsCredentialsProvider credentials;
    private final String bucket;
    private final String base;

    S3SnapshotStore(URI target, AppSettings settings) {
        bucket = target.getHost();
        String path = target.getPath().replaceFirst("^/", "");
        base = path.isEmpty() || path.endsWith("/") ? path : path + "/";
        credentials = settings.awsAuthMethod() == AppSettings.AwsAuthMethod.profile
            ? ProfileCredentialsProvider.create(settings.awsProfile())
            : StaticCredentialsProvider.create(AwsBasicCredentials.create(settings.awsAccessKeyId(), settings.awsSecretAccessKey()));
        client = S3Client.builder().region(Region.of(settings.awsRegion())).credentialsProvider(credentials).build();
    }

    // Package-visible for tests against the SDK boundary, without contacting AWS.
    S3SnapshotStore(S3Client client, String bucket, String base) {
        this.client = client;
        this.credentials = null;
        this.bucket = bucket;
        this.base = base;
    }

    @Override
    public boolean reserve(long epochMillis) {
        try {
            client.putObject(b -> b.bucket(bucket).key(base + "snapshots/" + epochMillis + "/.reservation")
                .ifNoneMatch("*"), RequestBody.empty());
            return true;
        } catch (S3Exception e) {
            if (e.statusCode() == 412 || e.statusCode() == 409) return false;
            throw e;
        }
    }

    @Override
    public List<Long> snapshotIds() {
        String prefix = base + "snapshots/";
        var ids = new ArrayList<Long>();
        var request = ListObjectsV2Request.builder().bucket(bucket).prefix(prefix).delimiter("/").build();
        for (var page : client.listObjectsV2Paginator(request)) {
            for (var common : page.commonPrefixes()) {
                String id = common.prefix().substring(prefix.length()).replaceFirst("/$", "");
                if (id.matches("[0-9]{1,18}")) ids.add(Long.parseLong(id));
            }
        }
        return ids;
    }

    @Override
    public InputStream open(String key) {
        return client.getObject(b -> b.bucket(bucket).key(base + key));
    }

    @Override
    public boolean exists(String key) {
        try {
            client.headObject(b -> b.bucket(bucket).key(base + key));
            return true;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) return false;
            throw e;
        }
    }

    @Override
    public void put(String key, Path file) throws IOException {
        if (Files.size(file) <= PART_SIZE) {
            client.putObject(b -> b.bucket(bucket).key(base + key), RequestBody.fromFile(file));
            return;
        }
        String upload = client.createMultipartUpload(b -> b.bucket(bucket).key(base + key)).uploadId();
        try (var input = Files.newInputStream(file)) {
            var parts = new ArrayList<CompletedPart>();
            byte[] bytes;
            while ((bytes = input.readNBytes(PART_SIZE)).length != 0) {
                int number = parts.size() + 1;
                if (number > 10_000)
                    throw new IOException("Archive exceeds the supported multipart size (about 156 GiB)");
                var response = client.uploadPart(b -> b.bucket(bucket).key(base + key).uploadId(upload)
                    .partNumber(number), RequestBody.fromBytes(bytes));
                parts.add(CompletedPart.builder().partNumber(number).eTag(response.eTag()).build());
            }
            client.completeMultipartUpload(b -> b.bucket(bucket).key(base + key).uploadId(upload)
                .multipartUpload(m -> m.parts(parts)));
        } catch (Exception failure) {
            try {
                client.abortMultipartUpload(b -> b.bucket(bucket).key(base + key).uploadId(upload));
            } catch (Exception cleanup) {
                failure.addSuppressed(cleanup);
            }
            throw failure;
        }
    }

    @Override
    public void remove(String prefix) {
        var request = ListObjectsV2Request.builder().bucket(bucket).prefix(base + prefix).build();
        for (var page : client.listObjectsV2Paginator(request))
            for (var object : page.contents()) client.deleteObject(b -> b.bucket(bucket).key(object.key()));
    }

    @Override
    public void close() {
        client.close();
        if (credentials instanceof ProfileCredentialsProvider profile) profile.close();
    }
}
