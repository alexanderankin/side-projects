package info.ankin.projects.tfe4j.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface Wrappers {

    @Data
    @Accessors(chain = true)
    class Single<T> {
        Item<T> data;
    }

    @Data
    @Accessors(chain = true)
    class Multiple<T> {
        List<Item<T>> data;
        Links links;
        Meta meta;
    }

    @Data
    @Accessors(chain = true)
    class Item<T> {
        String id;
        String type;
        T attributes;
        LinkedHashMap<String, Relationship<?>> relationships;
        Links links;
        Meta meta;

        public Single<T> toSingle() {
            throw new UnsupportedOperationException("serializing this item to a single is not implemented: " + this);
        }
    }

    @Data
    @Accessors(chain = true)
    class Links {
        String self;
        String first;
        String prev;
        String next;
        String last;
        String related;
    }

    @Data
    @Accessors(chain = true)
    class Meta {
        Pagination pagination;
        // for list results?
        @JsonProperty("status-counts")
        Map<String, Integer> statusCounts;

        // temporarily reconsidering after seeing one list in admin/org and other in admin/runs
        @Data
        @Accessors(chain = true)
        public static class StatusCounts {
            Integer total;
            Integer active;
            Integer disabled;
        }
    }

    @Data
    @Accessors(chain = true)
    class Pagination {
        @JsonProperty("current-page")
        Integer currentPage;
        @JsonProperty("prev-page")
        Integer prevPage;
        @JsonProperty("next-page")
        Integer nextPage;
        @JsonProperty("total-pages")
        Integer totalPages;
        @JsonProperty("total-count")
        Integer totalCount;
    }

    // todo verify this - just copied item and changed attributes to data.
    @Data
    @Accessors(chain = true)
    class Relationship<T> {
        String id;
        String type;
        T data;
        LinkedHashMap<String, Item<?>> relationships;
        Links links;
        Meta meta;
    }
}
