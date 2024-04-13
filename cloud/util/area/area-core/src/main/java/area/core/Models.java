package area.core;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

public interface Models {
    @Data
    @Accessors(chain = true)
    class Page<T> {
        List<T> contents;
        String next;
        Integer page, total;
    }

    @Data
    @Accessors(chain = true)
    class Tag {
        String name;
        String value;
    }
}
