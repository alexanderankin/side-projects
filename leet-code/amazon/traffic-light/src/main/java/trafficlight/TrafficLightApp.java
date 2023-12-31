package trafficlight;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Sinks;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@SpringBootApplication
class TrafficLightApp {
    @SneakyThrows
    public static void main(String[] args) {
        SpringApplication.run(TrafficLightApp.class, args);
    }

    static class Models {
        /**
         * assumes intersection has three "phases":
         * one light green, one light yellow, and all are red.
         */
        enum LightPhase {
            RED, YELLOW, GREEN,
            ;
            public static final List<LightPhase> VALUES = Arrays.asList(values());
        }

        @Retention(RetentionPolicy.RUNTIME)
        @JacksonAnnotationsInside
        @JsonIgnoreProperties(ignoreUnknown = true)
        @interface Dto {
        }

        @Dto
        @Data
        @Accessors(chain = true)
        static class BaseEntity {
            UUID id;
            String name;
            String description;
            Instant createdAt;
            Instant updatedAt;
        }

        @EqualsAndHashCode(callSuper = true)
        @ToString(callSuper = true)
        @Data
        @Accessors(chain = true)
        static class Intersection extends BaseEntity {
            public static final Intersection DEFAULTS = new Intersection()
                    .setEnabled(true)
                    .setDefaultDurations(Map.of(
                            // not currently realistic, set for testing
                            LightPhase.GREEN, Duration.ofSeconds(5),
                            LightPhase.YELLOW, Duration.ofSeconds(2),
                            LightPhase.RED, Duration.ofSeconds(2)
                    ));
            Boolean enabled;
            @NotEmpty
            Map<LightPhase, Duration> defaultDurations;

            /**
             * in order of green first when it restarts (assume green is mutex)
             */
            List<UUID> trafficLightIds;

            /**
             * todo left off here
             * <p>
             * cannot store this as an instant because Instant.now() is not monotonic.
             * <p>
             * However, if we store the nanoTime() + the relative to epochMilli at init,
             * then we can reasonably recover (or decide to reset this intersection.
             */
            transient Instant lastTransition;

            /**
             * or yellow.
             */
            transient UUID currentGreen;
        }

        @EqualsAndHashCode(callSuper = true)
        @ToString(callSuper = true)
        @Data
        @Accessors(chain = true)
        static class TrafficLight extends BaseEntity {
            Map<LightPhase, Duration> durations;
            Boolean enabled;
            UUID intersectionId;
            transient LightPhase currentPhase;
        }
    }

    @Service
    static class IntersectionService {
        final Map<UUID, Models.Intersection> intersections = Collections.synchronizedMap(new HashMap<>());
        final Map<UUID, Models.TrafficLight> trafficLights = Collections.synchronizedMap(new HashMap<>());
        final Map<UUID, Sinks.Many<Models.LightPhase>> trafficLightSinks = Collections.synchronizedMap(new HashMap<>());
        final Thread thread;

        IntersectionService() {
            // In the future, only do this when leader, or leader for an intersection
            thread = Thread.ofVirtual().name("matchStateLoop").start(this::matchStateLoop);
        }

        @SuppressWarnings("BusyWait")
        private void matchStateLoop() {
            while (true) {
                long l = System.nanoTime();
                matchState();
                long l1 = System.nanoTime();
                try {
                    Thread.sleep(Math.max(0, 1000 - Duration.ofNanos(l1 - l).toMillis()));
                } catch (InterruptedException ignored) {
                    break;
                }
            }
        }

        private void matchState() {
            for (Models.Intersection value : intersections.values()) {
                // skip the disabled ones
                if (!Boolean.TRUE.equals(value.getEnabled())) continue;
                // skip newly created ones (no lights to manipulate)
                if (CollectionUtils.isEmpty(value.getTrafficLightIds())) continue;

                // skip if too early to move
                // Duration timeTilMove = Duration.between(value.lastTransition);
                // boolean shouldMove = timeTilMove.compareTo(Duration.ofSeconds(1)) < 0;
                throw new UnsupportedOperationException("todo left off here - see lastTransition");
            }
        }

        public Models.Intersection create(Models.Intersection intersection) {
            // do not let the client overwrite data
            intersection.setId(UUID.randomUUID());
            // set up last transition
            intersection.setLastTransition(Instant.now());
            defaults(intersection);
            // persist the data
            intersections.put(intersection.getId(), intersection);
            return intersection;
        }

        private void defaults(Models.Intersection intersection) {
            if (null == intersection.getEnabled())
                intersection.setEnabled(Models.Intersection.DEFAULTS.getEnabled());
            if (null == intersection.getDefaultDurations())
                intersection.setDefaultDurations(Models.Intersection.DEFAULTS.getDefaultDurations());

            var durations = intersection.getDefaultDurations();
            var defaultDurations = Models.Intersection.DEFAULTS.getDefaultDurations();
            for (Models.LightPhase lightPhase : Models.LightPhase.VALUES) {
                if (null == durations.get(lightPhase)) {
                    durations.put(lightPhase, defaultDurations.get(lightPhase));
                }
            }

            if (null == intersection.getTrafficLightIds())
                intersection.setTrafficLightIds(Models.Intersection.DEFAULTS.getTrafficLightIds());
            if (null == intersection.getLastTransition())
                intersection.setLastTransition(Models.Intersection.DEFAULTS.getLastTransition());
            if (null == intersection.getCurrentGreen())
                intersection.setCurrentGreen(Models.Intersection.DEFAULTS.getCurrentGreen());
        }

    }

    // static class TrafficLightService {}

    @RequiredArgsConstructor
    @RestController
    @RequestMapping("/api")
    @Validated
    static class ApiController {
        final IntersectionService intersectionService;

        @PostMapping("/intersection")
        Models.Intersection createIntersection(@Valid @RequestBody Models.Intersection intersection) {
            return intersectionService.create(intersection);
        }
    }
}
