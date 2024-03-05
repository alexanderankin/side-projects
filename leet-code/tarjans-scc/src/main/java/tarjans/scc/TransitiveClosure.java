package tarjans.scc;

public class TransitiveClosure {
    private DirectedDFS[] tc;  // tc[v] = reachable from v

    /**
     * Computes the transitive closure of the digraph {@code G}.
     *
     * @param G the digraph
     */
    public TransitiveClosure(Digraph G) {
        tc = new DirectedDFS[G.V()];
        for (int v = 0; v < G.V(); v++)
            tc[v] = new DirectedDFS(G, v);
    }

    /**
     * Is there a directed path from vertex {@code v} to vertex {@code w} in the digraph?
     *
     * @param v the source vertex
     * @param w the target vertex
     * @return {@code true} if there is a directed path from {@code v} to {@code w},
     * {@code false} otherwise
     * @throws IllegalArgumentException unless {@code 0 <= v < V}
     * @throws IllegalArgumentException unless {@code 0 <= w < V}
     */
    public boolean reachable(int v, int w) {
        validateVertex(v);
        validateVertex(w);
        return tc[v].marked(w);
    }

    // throw an IllegalArgumentException unless {@code 0 <= v < V}
    private void validateVertex(int v) {
        int V = tc.length;
        if (v < 0 || v >= V)
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V - 1));
    }

    public static class DirectedDFS {
        private boolean[] marked;  // marked[v] = true iff v is reachable from source(s)
        private int count;         // number of vertices reachable from source(s)

        /**
         * Computes the vertices in digraph {@code G} that are
         * reachable from the source vertex {@code s}.
         *
         * @param G the digraph
         * @param s the source vertex
         * @throws IllegalArgumentException unless {@code 0 <= s < V}
         */
        public DirectedDFS(Digraph G, int s) {
            marked = new boolean[G.V()];
            validateVertex(s);
            dfs(G, s);
        }

        /**
         * Computes the vertices in digraph {@code G} that are
         * connected to any of the source vertices {@code sources}.
         *
         * @param G       the graph
         * @param sources the source vertices
         * @throws IllegalArgumentException if {@code sources} is {@code null}
         * @throws IllegalArgumentException if {@code sources} contains no vertices
         * @throws IllegalArgumentException unless {@code 0 <= s < V}
         *                                  for each vertex {@code s} in {@code sources}
         */
        public DirectedDFS(Digraph G, Iterable<Integer> sources) {
            marked = new boolean[G.V()];
            validateVertices(sources);
            for (int v : sources) {
                if (!marked[v]) dfs(G, v);
            }
        }

        private void dfs(Digraph G, int v) {
            count++;
            marked[v] = true;
            for (int w : G.adj(v)) {
                if (!marked[w]) dfs(G, w);
            }
        }

        /**
         * Is there a directed path from the source vertex (or any
         * of the source vertices) and vertex {@code v}?
         *
         * @param v the vertex
         * @return {@code true} if there is a directed path, {@code false} otherwise
         * @throws IllegalArgumentException unless {@code 0 <= v < V}
         */
        public boolean marked(int v) {
            validateVertex(v);
            return marked[v];
        }

        /**
         * Returns the number of vertices reachable from the source vertex
         * (or source vertices).
         *
         * @return the number of vertices reachable from the source vertex
         * (or source vertices)
         */
        public int count() {
            return count;
        }

        // throw an IllegalArgumentException unless {@code 0 <= v < V}
        private void validateVertex(int v) {
            int V = marked.length;
            if (v < 0 || v >= V)
                throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V - 1));
        }

        // throw an IllegalArgumentException if vertices is null, has zero vertices,
        // or has a vertex not between 0 and V-1
        private void validateVertices(Iterable<Integer> vertices) {
            if (vertices == null) {
                throw new IllegalArgumentException("argument is null");
            }
            int vertexCount = 0;
            for (Integer v : vertices) {
                vertexCount++;
                if (v == null) {
                    throw new IllegalArgumentException("vertex is null");
                }
                validateVertex(v);
            }
            if (vertexCount == 0) {
                throw new IllegalArgumentException("zero vertices");
            }
        }
    }
}
