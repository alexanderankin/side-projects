package org.example;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;

public class S3PresignedUrlExample {
    public static void main(String[] args) {
        String bucket = "";
        String key = "";

        try (S3Presigner presigner = S3Presigner.builder()
                .region(Region.US_EAST_1) // Set your region
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build()) {

            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(b -> b
                    .putObjectRequest(objectRequest)
                    .signatureDuration(Duration.ofMinutes(10))
            );

            System.out.println("Presigned URL: " + presignedRequest.url());
        }
    }
}
