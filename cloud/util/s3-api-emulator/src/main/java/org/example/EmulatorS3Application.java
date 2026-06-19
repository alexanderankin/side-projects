package org.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlWriteFeature;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@SpringBootApplication
public class EmulatorS3Application {
    static final XmlMapper XML_MAPPER = XmlMapper.builder()
            .findAndAddModules()
            .configure(XmlWriteFeature.WRITE_XML_DECLARATION, true)
            .build();
    static final JsonMapper JSON_MAPPER = JsonMapper.builder()
            .findAndAddModules()
            .build();

    public static void main(String[] args) {
        SpringApplication.run(EmulatorS3Application.class, args);
    }

    @RestController
    static class S3Controller {
        String myRegion = "us-east-1";
        Map<String, Bucket> buckets = new TreeMap<>();

        @SneakyThrows
        @RequestMapping(path = "/")
        String listBuckets(
                @RequestParam(name = "max-buckets", defaultValue = "10000") Integer maxBuckets,
                @Nullable @RequestParam("continuation-token") String continuationToken,
                @Nullable @RequestParam("prefix") String prefix,
                @Nullable @RequestParam("bucket-region") String bucketRegion
        ) {
            if (bucketRegion != null && !myRegion.equals(bucketRegion))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            var skip = StringUtils.hasText(continuationToken) ? Long.parseLong(continuationToken) : 0;
            var bucketInfos = buckets.values().stream()
                    .skip(skip)
                    .map(Bucket::getBucketInfo)
                    .filter(b -> prefix == null || b.getName().startsWith(prefix))
                    .limit(maxBuckets)
                    .toList();

            var resultCt = (skip + bucketInfos.size()) > buckets.size() ? String.valueOf(skip + bucketInfos.size()) : null;

            return XML_MAPPER.writeValueAsString(
                    new ListAllMyBucketsResult()
                            .setBuckets(bucketInfos)
                            .setOwner(new ListAllMyBucketsResult.Owner().setId("todo").setDisplayName("todo"))
                            .setContinuationToken(resultCt)
                            .setPrefix(prefix)
            );
        }

        @SneakyThrows
        @RequestMapping(path = "/{bucket}", method = RequestMethod.PUT)
        ResponseEntity<Void> createBucket(
                @Nullable @RequestHeader("x-amz-acl") String acl,
                @PathVariable String bucket,
                // CreateBucketConfiguration
                @Nullable @RequestBody String requestBody,
                @Nullable @RequestHeader("x-amz-grant-full-control") String grantFullControl,
                @Nullable @RequestHeader("x-amz-grant-read") String grantRead,
                @Nullable @RequestHeader("x-amz-grant-read-acp") String grantReadAcp,
                @Nullable @RequestHeader("x-amz-grant-write") String grantWrite,
                @Nullable @RequestHeader("x-amz-grant-write-acp") String grantWriteAcp,
                @Nullable @RequestHeader("x-amz-bucket-object-lock-enabled") String bucketObjectLockEnabled,
                @Nullable @RequestHeader("x-amz-object-ownership") String objectOwnership,
                @Nullable @RequestHeader("x-amz-bucket-namespace") String bucketNamespace
        ) {
            if (requestBody != null)
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT);

            var bucketArn = "arn:aws:s3:::" + bucket;
            buckets.put(bucket,
                    new Bucket().setBucketInfo(
                            new ListAllMyBucketsResult.Bucket()
                                    .setName(bucket)
                                    .setBucketArn(bucketArn)
                                    .setBucketRegion(myRegion)
                                    .setCreationDate(Instant.now())
                    ));

            return ResponseEntity.ok()
                    .header("Location", "/" + bucket)
                    .header("x-amz-bucket-arn", bucketArn)
                    .build();
        }

        @SneakyThrows
        @RequestMapping(path = "/{bucket}", method = RequestMethod.GET)
        String listObjects(
                @Nullable @RequestParam("delimiter") String delimiter,
                @Nullable @RequestParam("encoding-type") String encodingType,
                @Nullable @RequestParam("marker") String marker,
                @Nullable @RequestParam("max-keys") Integer maxKeys,
                @Nullable @RequestParam("prefix") String prefix,
                @PathVariable String bucket
        ) {
            var bucketEntity = buckets.get(bucket);
            if (bucketEntity == null)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            return "hi";
        }

        @SneakyThrows
        @RequestMapping(path = "/{bucket}", method = RequestMethod.GET, params = {"list-type=2"})
        String listObjectsV2(
                @Nullable @RequestParam("continuation-token") String continuationToken,
                @Nullable @RequestParam("delimiter") String delimiter,
                @Nullable @RequestParam("encoding-type") String encodingType,
                @Nullable @RequestParam("fetch-owner") String fetchOwner,
                @Nullable @RequestParam("max-keys") Integer maxKeys,
                @Nullable @RequestParam("prefix") String prefix,
                @Nullable @RequestParam("start-after") String startAfter,
                @PathVariable String bucket
        ) {
            return "hi";
        }

        @SneakyThrows
        @RequestMapping(
                method = {RequestMethod.GET, RequestMethod.HEAD},
                produces = MediaType.APPLICATION_XML_VALUE,
                path = "/{bucket}/{*path}"
        )
        Object s3Endpoint(HttpServletRequest request,
                          @PathVariable("bucket") String bucketName,
                          @PathVariable String path,
                          @RequestParam MultiValueMap<String, String> queryString) {
            var bucket = buckets.get(bucketName);
            if (bucket == null)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

            ListV2Params listV2Params = JSON_MAPPER.convertValue(queryString.toSingleValueMap(), ListV2Params.class);

            if (ListV2Params.LIST_TYPE_V2.equals(listV2Params.getListType())) {
                return XML_MAPPER.writeValueAsString(Map.of("ListBucketResult",
                        Map.of("IsTruncated", false,
                                "Contents", List.of(
                                        Map.of("ETag", "abc", "Size", 1),
                                        Map.of("ETag", "abc", "Size", 2)
                                ))
                ));
            }

            return null;
        }
    }


    @Data
    @Accessors(chain = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ListV2Params {
        static Integer LIST_TYPE_V2 = Integer.valueOf(2);

        @JsonProperty("list-type")
        Integer listType;

        String prefix;

        @JsonProperty("encoding-type")
        String encodingType;
    }

    @JacksonXmlRootElement(localName = "ListBucketResult")
    static class ListV2Response {
        @JacksonXmlProperty(localName = "IsTruncated")
        Boolean isTruncated;
    }
}
