package side.oci.helpers.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class BaseOciEntity {
    String id;
    String name;
    String description;
    @JsonProperty("defined-tags")
    Map<String, Map<String, String>> definedTags;
    @JsonProperty("freeform-tags")
    Map<String, String> freeFormTags;
}
