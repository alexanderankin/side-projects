package info.ankin.ratelimit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class Response {
    int xRateLimitLimit;
    int xRateLimitRemaining;
    long xRateLimitReset;
    int xRateLimitUsed;
    String xRateLimitResource;
}
