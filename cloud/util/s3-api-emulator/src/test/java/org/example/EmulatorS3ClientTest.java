package org.example;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.net.URI;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EmulatorS3ClientTest {

    @LocalServerPort
    Integer localServerPort;

    @Test
    void test() {
        @SuppressWarnings("resource")
        S3Client s3Client = S3Client.builder()
                .credentialsProvider(AnonymousCredentialsProvider.create())
                .forcePathStyle(true)
                .endpointOverride(URI.create("http://127.0.0.1:" + localServerPort))
                .region(Region.US_EAST_1)
                .build();

        ListBucketsResponse listBucketsResponse = s3Client.listBuckets(ListBucketsRequest.builder().build());
        System.out.println(listBucketsResponse);

        CreateBucketResponse createBucketResponse = s3Client.createBucket(CreateBucketRequest.builder()
                .bucket("test-bucket")
                .build());
        System.out.println(createBucketResponse);
        System.out.println(createBucketResponse.location());
        System.out.println(createBucketResponse.bucketArn());

        System.out.println(s3Client.listBuckets(ListBucketsRequest.builder().build()).buckets());
        try {
            System.out.println(s3Client.listObjects(ListObjectsRequest.builder().bucket("test-bucket").build()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        try {
            System.out.println(s3Client.listObjectsV2(ListObjectsV2Request.builder().bucket("test-bucket").build()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
