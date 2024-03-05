package tarjans.scc;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TarjanSCCTest {

    static List<DigraphTestCase> testCases() {
        return List.of(
                new DigraphTestCase(
                        new Digraph(
                                13,
                                22,
                                List.of(Map.entry(4, 2),
                                        Map.entry(2, 3),
                                        Map.entry(3, 2),
                                        Map.entry(6, 0),
                                        Map.entry(0, 1),
                                        Map.entry(2, 0),
                                        Map.entry(11, 12),
                                        Map.entry(12, 9),
                                        Map.entry(9, 10),
                                        Map.entry(9, 11),
                                        Map.entry(7, 9),
                                        Map.entry(10, 12),
                                        Map.entry(11, 4),
                                        Map.entry(4, 3),
                                        Map.entry(3, 5),
                                        Map.entry(6, 8),
                                        Map.entry(8, 6),
                                        Map.entry(5, 4),
                                        Map.entry(0, 5),
                                        Map.entry(6, 4),
                                        Map.entry(6, 9),
                                        Map.entry(7, 6))
                        ),
                        Set.of(Set.of(1), Set.of(9, 10, 11, 12), Set.of(6, 8), Set.of(0, 2, 3, 4, 5), Set.of(7))
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void test(DigraphTestCase testCase) {
        Digraph digraph = testCase.digraph();
        Set<Set<Integer>> expected = testCase.expected();
        assertEquals(expected, new TarjanSCC(digraph).result());
    }

    record DigraphTestCase(Digraph digraph, Set<Set<Integer>> expected) {}
}
