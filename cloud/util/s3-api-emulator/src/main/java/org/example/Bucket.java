package org.example;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.TreeMap;

@Data
@Accessors(chain = true)
public class Bucket {
    ListAllMyBucketsResult.Bucket bucketInfo;
    Map<String, BucketObject> objectMap = new TreeMap<>();

    @Data
    @Accessors(chain = true)
    public static class BucketObject {
        byte[] content;
    }
}
