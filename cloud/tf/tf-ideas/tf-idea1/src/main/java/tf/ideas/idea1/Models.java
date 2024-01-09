package tf.ideas.idea1;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface Models {
    @Data
    @Accessors(chain = true)
    class State {
        UUID id;
        Integer version;
        List<Output> outputs;
        List<Resource> resources;
    }

    @Data
    @Accessors(chain = true)
    class Output {
        String name;
        Map<String, Object> value;
    }

    @Data
    @Accessors(chain = true)
    class Resource {
        Type type;
        String resourceType;
        String name;

        enum Type {
            DATA, MANAGED
        }
    }
}
