package side.learning;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

class ByteArrayOperations {
    static final VarHandle LONG_VIEW =
            MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.nativeOrder());

    static void reverse(byte[] a) {
        if (a == null || a.length <= 1) return;

        int left = 0;
        int right = a.length;

        // Process 8 bytes at a time from both ends
        while (right - left >= 16) {
            right -= 8;

            long lv = (long) LONG_VIEW.get(a, left);
            long rv = (long) LONG_VIEW.get(a, right);

            lv = Long.reverseBytes(lv);
            rv = Long.reverseBytes(rv);

            LONG_VIEW.set(a, left, rv);
            LONG_VIEW.set(a, right, lv);

            left += 8;
        }

        // Scalar cleanup
        int r = a.length - 1;
        while (left < r) {
            byte tmp = a[left];
            a[left] = a[r];
            a[r] = tmp;
            left++;
            r--;
        }
    }

    static void and(byte[] a, int aLen, byte[] b, int bLen) {
        int min = Math.min(aLen, bLen);

        int i = 0;
        int limit = min & ~7; // largest multiple of 8 ≤ min

        // 8-byte chunks
        while (i < limit) {
            long av = (long) LONG_VIEW.get(a, i);
            long bv = (long) LONG_VIEW.get(b, i);
            LONG_VIEW.set(a, i, av & bv);
            i += 8;
        }

        // tail
        while (i < min) {
            a[i] &= b[i];
            i++;
        }

        // zero the rest of a
        for (int j = min; j < aLen; j++) {
            a[j] = 0;
        }
    }

    static void or(byte[] a, int aLen, byte[] b, int bLen) {
        int min = Math.min(aLen, bLen);

        int i = 0;
        int limit = min & ~7; // largest multiple of 8 ≤ min

        // 8-byte chunks
        while (i < limit) {
            long av = (long) LONG_VIEW.get(a, i);
            long bv = (long) LONG_VIEW.get(b, i);
            LONG_VIEW.set(a, i, av | bv);
            i += 8;
        }

        // tail
        while (i < min) {
            a[i] |= b[i];
            i++;
        }

        // zero the rest of a
        for (int j = min; j < aLen; j++) {
            a[j] = 0;
        }
    }
}
