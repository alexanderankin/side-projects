package org.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;
import lombok.experimental.Accessors;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.time.Instant;
import java.util.List;

@Data
@Accessors(chain = true)
@JsonRootName("ListAllMyBucketsResult")
public class ListAllMyBucketsResult {
    @JsonProperty("Buckets")
    @JacksonXmlElementWrapper(localName = "Buckets")
    @JacksonXmlProperty(localName = "Bucket")
    private List<Bucket> buckets;

    @JsonProperty("Owner")
    private Owner owner;

    @JsonProperty("ContinuationToken")
    private String continuationToken;

    @JsonProperty("Prefix")
    private String prefix;

    @Data
    @Accessors(chain = true)
    public static class Bucket {
        @JsonProperty("BucketArn")
        private String bucketArn;

        @JsonProperty("BucketRegion")
        private String bucketRegion;

        @JsonProperty("CreationDate")
        private Instant creationDate;

        @JsonProperty("Name")
        private String name;
    }

    @Data
    @Accessors(chain = true)
    public static class Owner {
        @JsonProperty("DisplayName")
        private String displayName;

        @JsonProperty("ID")
        private String id;
    }
}
