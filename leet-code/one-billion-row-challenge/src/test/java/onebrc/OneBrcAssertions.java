package onebrc;

import java.util.Map;
import java.util.Objects;
import java.util.SequencedMap;

import static org.junit.jupiter.api.Assertions.*;

public class OneBrcAssertions {
    public static void assertSequencedMapEquals(Map<?, ?> expected, Map<?, ?> actual) {
        assertInstanceOf(SequencedMap.class, expected);
        assertInstanceOf(SequencedMap.class, actual);
        assertSequencedMapEquals((SequencedMap<?, ?>) expected, (SequencedMap<?, ?>) actual);
    }

    public static void assertSequencedMapEquals(SequencedMap<?, ?> expected, SequencedMap<?, ?> actual) {
        assertNotNull(expected, "expected cannot be null for assertSequencedMapEquals");
        assertNotNull(actual, "actual cannot be null for assertSequencedMapEquals");
        assertEquals(expected.size(), actual.size(),
                () -> "expected (%d) and actual (%d) different sizes for assertSequencedMapEquals"
                        .formatted(expected.size(), actual.size()));

        var eIt = expected.entrySet().iterator();
        var aIt = actual.entrySet().iterator();

        while (eIt.hasNext()) {
            assertEquals(eIt.next(), aIt.next());
        }
    }

    public static void assertSequencedMapNotEquals(Map<?, ?> expected, Map<?, ?> actual) {
        assertInstanceOf(SequencedMap.class, expected);
        assertInstanceOf(SequencedMap.class, actual);
        assertSequencedMapNotEquals((SequencedMap<?, ?>) expected, (SequencedMap<?, ?>) actual);
    }

    public static void assertSequencedMapNotEquals(SequencedMap<?, ?> expected, SequencedMap<?, ?> actual) {
        assertNotNull(expected, "expected cannot be null for assertSequencedMapEquals");
        assertNotNull(actual, "actual cannot be null for assertSequencedMapEquals");
        assertEquals(expected.size(), actual.size(),
                () -> "expected (%d) and actual (%d) different sizes for assertSequencedMapNotEquals"
                        .formatted(expected.size(), actual.size()));

        var eIt = expected.entrySet().iterator();
        var aIt = actual.entrySet().iterator();

        while (eIt.hasNext() && aIt.hasNext()) {
            if (!Objects.equals(eIt.next(), aIt.next())) return;
        }

        fail("all of the elements were the same for assertSequencedMapNotEquals");
    }
}
