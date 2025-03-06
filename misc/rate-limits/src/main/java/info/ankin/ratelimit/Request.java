package info.ankin.ratelimit;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Request {
    String principal;
    String resourceName;
}
