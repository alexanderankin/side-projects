package tarjans.scc;

import java.util.*;

public class ArraySet<T> extends AbstractSet<T> {
    private final ArrayList<T> e;

    public ArraySet(ArrayList<T> e) {
        this.e = e;
    }

    @Override
    public Iterator<T> iterator() {
        return e.iterator();
    }

    @Override
    public int size() {
        return e.size();
    }
}
