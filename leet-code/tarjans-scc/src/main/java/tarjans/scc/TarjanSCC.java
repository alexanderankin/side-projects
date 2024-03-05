package tarjans.scc;

import java.util.*;
import java.util.stream.Collectors;

/******************************************************************************
 *  Compilation:  javac TarjanSCC.java
 *  Execution:    Java TarjanSCC V E
 *  Dependencies: Digraph.java Stack.java TransitiveClosure.java StdOut.java
 *  Data files:   https://algs4.cs.princeton.edu/42digraph/tinyDG.txt
 *                https://algs4.cs.princeton.edu/42digraph/mediumDG.txt
 *                https://algs4.cs.princeton.edu/42digraph/largeDG.txt
 * <p>
 *  Compute the strongly-connected components of a digraph using
 *  Tarjan's algorithm.
 * <p>
 *
 *  Runs in O(E + V) time.
 * <p>
 *
 *  % java TarjanSCC tinyDG.txt
 *  5 components
 *  1
 *  0 2 3 4 5
 *  9 10 11 12
 *  6 8
 *  7
 * <p>
 *
 *  The {@code TarjanSCC} class represents a data type for
 *  determining the strong components in a digraph.
 *  The <em>id</em> operation determines in which strong component
 *  a given vertex lies; the <em>areStronglyConnected</em> operation
 *  determines whether two vertices are in the same strong component;
 *  and the <em>count</em> operation determines the number of strong
 *  components.
 *  <p>
 *  The <em>component identifier</em> of a component is one of the
 *  vertices in the strong component: two vertices have the same component
 *  identifier if and only if they are in the same strong component.
 *  <p>
 *  This implementation uses Tarjan's algorithm.
 *  The constructor takes &Theta;(<em>V</em> + <em>E</em>) time,
 *  where <em>V</em> is the number of vertices and <em>E</em> is the
 *  number of edges.
 *  Each instance method takes &Theta;(1) time.
 *  It uses &Theta;(<em>V</em>) extra space (not including the digraph).
 *  For alternative implementations of the same API, see
 *  {@link KosarajuSharirSCC} and {@link GabowSCC}.
 *  <p>
 *  For additional documentation,
 *  see <a href="https://algs4.cs.princeton.edu/42digraph">Section 4.2</a> of
 *  <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *
 *  @author Robert Sedgewick
 *  @author Kevin Wayne
 */
public class TarjanSCC {

    private final Digraph g;
    private final boolean[] marked;        // marked[v] = has v been visited?
    private final int[] id;                // id[v] = id of strong component containing v
    private final int[] low;               // low[v] = low number of v
    private int pre;                 // preorder number counter
    private int count;               // number of strongly-connected components
    private final Stack<Integer> stack;


    /**
     * Computes the strong components of the digraph {@code G}.
     *
     * @param G the digraph
     */
    public TarjanSCC(Digraph G) {
        g = G;
        marked = new boolean[G.V()];
        stack = new Stack<>();
        id = new int[G.V()];
        low = new int[G.V()];
        for (int v = 0; v < G.V(); v++) {
            if (!marked[v]) dfs(G, v);
        }

        // check that id[] gives strong components
        assert check(G);
    }

    private void dfs(Digraph G, int v) {
        marked[v] = true;
        low[v] = pre++;
        int min = low[v];
        stack.push(v);
        for (int w : G.adj(v)) {
            if (!marked[w]) dfs(G, w);
            if (low[w] < min) min = low[w];
        }
        if (min < low[v]) {
            low[v] = min;
            return;
        }
        int w;
        do {
            w = stack.pop();
            id[w] = count;
            low[w] = G.V();
        } while (w != v);
        count++;
    }


    /**
     * Returns the number of strong components.
     *
     * @return the number of strong components
     */
    public int count() {
        return count;
    }


    /**
     * Are vertices {@code v} and {@code w} in the same strong component?
     *
     * @param v one vertex
     * @param w the other vertex
     * @return {@code true} if vertices {@code v} and {@code w} are in the same
     * strong component, and {@code false} otherwise
     * @throws IllegalArgumentException unless {@code 0 <= v < V}
     * @throws IllegalArgumentException unless {@code 0 <= w < V}
     */
    public boolean stronglyConnected(int v, int w) {
        validateVertex(v);
        validateVertex(w);
        return id[v] == id[w];
    }

    /**
     * Returns the component id of the strong component containing vertex {@code v}.
     *
     * @param v the vertex
     * @return the component id of the strong component containing vertex {@code v}
     * @throws IllegalArgumentException unless {@code 0 <= v < V}
     */
    public int id(int v) {
        validateVertex(v);
        return id[v];
    }

    // does the id[] array contain the strongly connected components?
    private boolean check(Digraph G) {
        TransitiveClosure tc = new TransitiveClosure(G);
        for (int v = 0; v < G.V(); v++) {
            for (int w = 0; w < G.V(); w++) {
                if (stronglyConnected(v, w) != (tc.reachable(v, w) && tc.reachable(w, v)))
                    return false;
            }
        }
        return true;
    }

    // throw an IllegalArgumentException unless {@code 0 <= v < V}
    private void validateVertex(int v) {
        int V = marked.length;
        if (v < 0 || v >= V)
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V - 1));
    }

    public Set<Set<Integer>> result() {
        // number of connected components
        var m = count();

        // compute list of vertices in each strong component
        // noinspection unchecked
        Queue<Integer>[] components = (Queue<Integer>[]) new Queue[m];
        for (int i = 0; i < m; i++) {
            components[i] = new ArrayDeque<>();
        }
        for (int v = 0; v < g.V(); v++) {
            components[this.id(v)].add(v);
        }

        return Arrays.stream(components)
                .map(e -> new ArraySet<>(new ArrayList<>(e)))
                .collect(Collectors.toSet());
    }

    /**
     * Unit tests the {@code TarjanSCC} data type.
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        // In in = new In(args[0]);
        Digraph G = new Digraph(
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
        );
        var scc = new TarjanSCC(G).result();
        System.out.println(scc);

    }

}
