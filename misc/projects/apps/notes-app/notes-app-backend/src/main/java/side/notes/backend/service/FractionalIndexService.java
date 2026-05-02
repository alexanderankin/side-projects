package side.notes.backend.service;

import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * this is code from <a href=https://github.com/rocicorp/fractional-indexing>rocicorp/fractional-indexing</a>
 */
@Service
public class FractionalIndexService {
    private static final byte[] BASE_62_DIGITS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".getBytes();

    // byte[] midpoint(byte[] a, ByteBuffer b, byte[] digits) {
    byte[] midpoint(byte[] a, byte[] b, byte[] digits) {
        var zero = digits[0];
        if (b != null && Utils.compareTo(a, b) >= 0) {
            throw new IllegalArgumentException(new String(a) + " >= " + new String(b));
            // throw new IllegalArgumentException(new String(a) + " >= " + b.asCharBuffer());
        }
        // if ((a.length > 0 && a[a.length - 1] == zero) || (b != null && b.get(b.limit() - 1) == zero)) {
        if ((a.length > 0 && a[a.length - 1] == zero) || (b != null && b[b.length - 1] == zero)) {
            throw new IllegalArgumentException("trailing zero");
        }
        if (b != null) {
            // remove longest common prefix.  pad `a` with 0s as we
            // go.  note that we don't need to pad `b`, because it can't
            // end before `a` while traversing the common prefix.
            var n = 0;
            // while (((n >= a.length ? 0 : a[n]) | zero) == (n >= b.limit() ? 0 : b[n])) {
            while (((n >= a.length ? 0 : a[n]) | zero) == (n >= b.length ? 0 : b[n])) {
                n++;
            }
            if (n > 0) {
                return Utils.concat(
                        Arrays.copyOf(b, n),
                        // b.slice(0, n),
                        midpoint(Arrays.copyOfRange(a, n, a.length), Arrays.copyOfRange(b, n, b.length), digits)
                        // midpoint(Arrays.copyOfRange(a, n, a.length), Arrays.copyOfRange(b, n, b.limit()), digits)
                );
            }
        }
        // first digits (or lack of digit) are different
        int digitA = a.length > 0 ? Arrays.binarySearch(digits, a[0]) : 0;
        int digitB = b != null ? Arrays.binarySearch(digits, b[0]) : digits.length;
        if (digitB - digitA > 1) {
            int midDigit = (int) Math.round(0.5 * (digitA + digitB));
            return Arrays.copyOfRange(digits, midDigit, midDigit + 1);
        } else {
            // first digits are consecutive
            // if (b != null && b.limit() > 1) {
            if (b != null && b.length > 1) {
                return Arrays.copyOfRange(b, 0, 1);
            } else {
                // `b` is null or has length 1 (a single digit).
                // the first digit of `a` is the previous digit to `b`,
                // or 9 if `b` is null.
                // given, for example, midpoint('49', '5'), return
                // '4' + midpoint('9', null), which will become
                // '4' + '9' + midpoint('', null), which is '495'
                return Utils.concat(
                        Arrays.copyOfRange(digits, digitA, digitA + 1),
                        midpoint(Arrays.copyOfRange(a, a.length == 0 ? 0 : 1, a.length), null, digits)
                );
            }
        }
    }

    void validateInteger(byte[] input) {
        if (input.length != getIntegerLength(input[0])) {
            throw new IllegalArgumentException("invalid integer part of order key: " + new String(input));
        }
    }

    int getIntegerLength(byte head0) {
        if (head0 >= 'a' && head0 <= 'z') {
            return head0 - 'a' + 2;
        } else if (head0 >= 'A' && head0 <= 'Z') {
            return 'Z' - head0 + 2;
        }
        throw new IllegalArgumentException("invalid order key head: " + (char) head0);
    }

    byte[] getIntegerPart(byte[] key) {
        var integerPartLength = getIntegerLength(key[0]);
        if (integerPartLength > key.length) {
            throw new IllegalArgumentException("invalid order key: " + new String(key));
        }
        return Arrays.copyOfRange(key, 0, integerPartLength);
    }

    void validateOrderKey(byte[] key, byte[] digits) {
        // byte[] zeroes = new byte[26];
        // Arrays.fill(zeroes, digits[0]);
        // if (Utils.compareTo(key, Utils.concat(new byte[]{'A'}, zeroes)) == 0) {
        //     throw new IllegalArgumentException("invalid order key: " + new String(key));
        // }

        boolean keyIsNotSmallest;
        {
            if (!(key.length == 27 && key[0] == 'A')) {
                keyIsNotSmallest = false;
            } else {
                boolean allZero = true;
                for (int i = 0; i < 26; i++) {
                    if (key[i + 1] != digits[0]) {
                        allZero = false;
                        break;
                    }
                }
                keyIsNotSmallest = allZero;
            }
        }
        if (keyIsNotSmallest)
            throw new IllegalArgumentException("invalid order key: " + new String(key));

        var i = getIntegerPart(key);
        if (i.length < key.length && key[key.length - 1] == digits[0]) {
            throw new IllegalArgumentException("invalid order key: " + new String(key));
        }
    }

    byte[] incrementInteger(byte[] x, byte[] digits) {
        validateInteger(x);
        var head = x[0];
        var output = Arrays.copyOf(x, x.length);
        // var digs = Arrays.copyOfRange(x, 1, x.length);
        var carry = true;
        // for (int i = digs.length - 1; carry && i >= 0; i--) {
        for (int i = output.length - 2; carry && i >= 0; i--) {
            // var dOld = Arrays.binarySearch(digits, digs[i]) + 1;
            var d = Arrays.binarySearch(digits, output[i + 1]) + 1;
            if (d == digits.length) {
                // digs[i] = digits[0];
                output[i + 1] = digits[0];
            } else {
                // digs[i] = digits[d];
                output[i + 1] = digits[d];
                carry = false;
            }
        }
        if (carry) {
            if (head == 'Z') {
                return new byte[]{'a', digits[0]};
            }
            if (head == 'z') {
                return null;
            }
            var h = (byte) (head + 1);
            if (h > 'a') {
                // digs = Arrays.copyOf(digs, digs.length + 1);
                // digs[digs.length - 1] = digits[0];
                output = Arrays.copyOf(output, output.length + 1);
                output[output.length - 1] = digits[0];
            } else {
                // digs = Arrays.copyOf(digs, digs.length - 1);
            }
            // return Utils.concat(new byte[]{h}, digs);
            output[0] = h;
            return output;
        }
        // return Utils.concat(new byte[]{head}, digs);
        output[0] = head;
        return output;
    }

    byte[] decrementInteger(byte[] x, byte[] digits) {
        validateInteger(x);
        var head = x[0];
        var digs = Arrays.copyOfRange(x, 1, x.length);
        var borrow = true;
        for (int i = digs.length - 1; borrow && i >= 0; i--) {
            var d = Arrays.binarySearch(digits, digs[i]) - 1;
            if (d == -1) {
                digs[i] = digits[digits.length - 1];
            } else {
                digs[i] = digits[d];
                borrow = false;
            }
        }
        if (borrow) {
            if (head == 'a') {
                return new byte[]{'Z', digits[digits.length - 1]};
            }
            if (head == 'A') {
                return null;
            }
            var h = (byte) (head - 1);
            if (h < 'Z') {
                digs = Arrays.copyOf(digs, digs.length + 1);
                digs[digs.length - 1] = digits[digits.length - 1];
            } else {
                digs = Arrays.copyOf(digs, digs.length - 1);
            }
            return Utils.concat(new byte[]{h}, digs);
        }
        return Utils.concat(new byte[]{head}, digs);
    }

    public byte[] generateKeyBetween(byte[] a, byte[] b) {
        return generateKeyBetween(a, b, BASE_62_DIGITS);
    }

    public byte[] generateKeyBetween(byte[] a, byte[] b, byte[] digits) {
        if (a != null) validateOrderKey(a, digits);
        if (b != null) validateOrderKey(b, digits);

        if (a != null && b != null && Utils.compareTo(a, b) >= 0) {
            throw new IllegalArgumentException(new String(a) + " >= " + new String(b));
        }

        if (a == null) {
            if (b == null) {
                return new byte[]{'a', digits[0]};
            }

            byte[] ib = getIntegerPart(b);
            var fb = Arrays.copyOfRange(b, ib.length, b.length);
            // var fb = ByteBuffer.wrap(b, ib.length, b.length);
            // byte[] smallest = Utils.concat(new byte[]{'A'}, new byte[26]);
            // Arrays.fill(smallest, 1, smallest.length, digits[0]);
            // var ibIsSmallest = Utils.compareTo(ib, smallest) == 0;
            boolean ibIsSmallest;
            {
                if (!(ib.length == 27 && ib[0] == 'A')) {
                    ibIsSmallest = false;
                } else {
                    boolean allZero = true;
                    for (int i = 0; i < 26; i++) {
                        if (ib[i + 1] != digits[0]) {
                            allZero = false;
                            break;
                        }
                    }
                    ibIsSmallest = allZero;
                }
            }

            if (ibIsSmallest) {
                return Utils.concat(ib, midpoint(new byte[0], fb, digits));
            }

            if (Utils.compareTo(ib, b) < 0) {
                return ib;
            }

            byte[] res = decrementInteger(ib, digits);
            if (res == null) {
                throw new IllegalArgumentException("cannot decrement any more");
            }
            return res;
        }

        if (b == null) {
            byte[] ia = getIntegerPart(a);
            byte[] fa = Arrays.copyOfRange(a, ia.length, a.length);
            byte[] i = incrementInteger(ia, digits);
            return i == null ? Utils.concat(ia, midpoint(fa, null, digits)) : i;
        }

        byte[] ia = getIntegerPart(a);
        byte[] fa = Arrays.copyOfRange(a, ia.length, a.length);
        byte[] ib = getIntegerPart(b);
        byte[] fb = Arrays.copyOfRange(b, ib.length, b.length);

        if (Utils.compareTo(ia, ib) == 0) {
            return Utils.concat(ia, midpoint(fa, fb, digits));
        }

        byte[] i = incrementInteger(ia, digits);
        if (i == null) {
            throw new IllegalArgumentException("cannot increment any more");
        }

        if (Utils.compareTo(i, b) < 0) {
            return i;
        }

        return Utils.concat(ia, midpoint(fa, null, digits));
    }

    public byte[][] generateNKeysBetween(byte[] a, byte[] b, int n) {
        return generateNKeysBetween(a, b, n, BASE_62_DIGITS);
    }

    public byte[][] generateNKeysBetween(byte[] a, byte[] b, int n, byte[] digits) {
        if (n == 0) {
            return new byte[0][];
        }

        if (n == 1) {
            return new byte[][]{generateKeyBetween(a, b, digits)};
        }

        if (b == null) {
            byte[][] result = new byte[n][];
            byte[] c = generateKeyBetween(a, null, digits);
            result[0] = c;
            for (int i = 1; i < n; i++) {
                c = generateKeyBetween(c, null, digits);
                result[i] = c;
            }
            return result;
        }

        if (a == null) {
            byte[][] result = new byte[n][];
            byte[] c = generateKeyBetween(null, b, digits);
            result[0] = c;
            for (int i = 1; i < n; i++) {
                c = generateKeyBetween(null, c, digits);
                result[i] = c;
            }
            for (int i = 0; i < n / 2; i++) {
                byte[] tmp = result[i];
                result[i] = result[n - 1 - i];
                result[n - 1 - i] = tmp;
            }
            return result;
        }

        int mid = n / 2;
        byte[] c = generateKeyBetween(a, b, digits);
        byte[][] left = generateNKeysBetween(a, c, mid, digits);
        byte[][] right = generateNKeysBetween(c, b, n - mid - 1, digits);
        byte[][] result = new byte[n][];
        System.arraycopy(left, 0, result, 0, left.length);
        result[left.length] = c;
        System.arraycopy(right, 0, result, left.length + 1, right.length);
        return result;
    }

    public String generateKeyBetween(String a, String b) {
        return new String(
                generateKeyBetween(
                        a == null ? null : a.getBytes(),
                        b == null ? null : b.getBytes()
                )
        );
    }

    public String generateKeyBetween(String a, String b, String digits) {
        return new String(
                generateKeyBetween(
                        a == null ? null : a.getBytes(),
                        b == null ? null : b.getBytes(),
                        digits.getBytes()
                )
        );
    }

    public String[] generateNKeysBetween(String a, String b, int n) {
        byte[][] results = generateNKeysBetween(
                a == null ? null : a.getBytes(),
                b == null ? null : b.getBytes(),
                n
        );
        String[] result = new String[results.length];
        for (int i = 0; i < results.length; i++) {
            result[i] = new String(results[i]);
        }
        return result;
    }

    public String[] generateNKeysBetween(String a, String b, int n, String digits) {
        byte[][] results = generateNKeysBetween(
                a == null ? null : a.getBytes(),
                b == null ? null : b.getBytes(),
                n,
                digits.getBytes()
        );
        String[] result = new String[results.length];
        for (int i = 0; i < results.length; i++) {
            result[i] = new String(results[i]);
        }
        return result;
    }

    static class Utils {
        public static int compareTo(byte[] value, byte[] other) {
            return compareTo(value, other, value.length, other.length);
        }

        public static int compareTo(byte[] value, byte[] other, int len1, int len2) {
            int k = Arrays.mismatch(value, 0, len1, other, 0, len2);
            if (k < 0 || k == len1 || k == len2) {
                return len1 - len2;
            }
            return getChar(value, k) - getChar(other, k);
        }

        static char getChar(byte[] val, int index) {
            return (char) (val[index] & 0xff);
        }

        static char getChar(byte val) {
            return (char) (val & 0xff);
        }

        static byte[] concat(byte[] b0n, byte[] midpointResult) {
            byte[] result = new byte[b0n.length + midpointResult.length];
            System.arraycopy(b0n, 0, result, 0, b0n.length);
            System.arraycopy(midpointResult, 0, result, b0n.length, midpointResult.length);
            return result;
        }

        static int compareTo(byte[] a, ByteBuffer b) {
            int k = mismatch(a, b);
            if (k < 0 || k == a.length || k == b.remaining()) {
                return a.length - b.remaining();
            }
            return getChar(a, k) - getChar(b.get(k));
        }

        static int mismatch(byte[] a, ByteBuffer b) {
            var aLength = a.length;
            var bLength = b.remaining();
            var min = Math.min(aLength, bLength);
            for (int i = 0; i < min; i++) {
                if (a[i] != b.get(i)) {
                    return i;
                }
            }
            return aLength == bLength ? -1 : min;
        }
    }
}


// // License: CC0 (no rights reserved).
//
// // This is based on https://observablehq.com/@dgreensp/implementing-fractional-indexing
//
// export const BASE_62_DIGITS =
//   "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
//
// // `a` may be empty string, `b` is null or non-empty string.
// // `a < b` lexicographically if `b` is non-null.
// // no trailing zeros allowed.
// // digits is a string such as '0123456789' for base 10.  Digits must be in
// // ascending character code order!
// /**
//  * @param {string} a
//  * @param {string | null | undefined} b
//  * @param {string} digits
//  * @returns {string}
//  */
// function midpoint(a, b, digits) {
//   const zero = digits[0];
//   if (b != null && a >= b) {
//     throw new Error(a + " >= " + b);
//   }
//   if (a.slice(-1) === zero || (b && b.slice(-1) === zero)) {
//     throw new Error("trailing zero");
//   }
//   if (b) {
//     // remove longest common prefix.  pad `a` with 0s as we
//     // go.  note that we don't need to pad `b`, because it can't
//     // end before `a` while traversing the common prefix.
//     let n = 0;
//     while ((a[n] || zero) === b[n]) {
//       n++;
//     }
//     if (n > 0) {
//       return b.slice(0, n) + midpoint(a.slice(n), b.slice(n), digits);
//     }
//   }
//   // first digits (or lack of digit) are different
//   const digitA = a ? digits.indexOf(a[0]) : 0;
//   const digitB = b != null ? digits.indexOf(b[0]) : digits.length;
//   if (digitB - digitA > 1) {
//     const midDigit = Math.round(0.5 * (digitA + digitB));
//     return digits[midDigit];
//   } else {
//     // first digits are consecutive
//     if (b && b.length > 1) {
//       return b.slice(0, 1);
//     } else {
//       // `b` is null or has length 1 (a single digit).
//       // the first digit of `a` is the previous digit to `b`,
//       // or 9 if `b` is null.
//       // given, for example, midpoint('49', '5'), return
//       // '4' + midpoint('9', null), which will become
//       // '4' + '9' + midpoint('', null), which is '495'
//       return digits[digitA] + midpoint(a.slice(1), null, digits);
//     }
//   }
// }
//
// /**
//  * @param {string} int
//  * @return {void}
//  */
//
// function validateInteger(int) {
//   if (int.length !== getIntegerLength(int[0])) {
//     throw new Error("invalid integer part of order key: " + int);
//   }
// }
//
// /**
//  * @param {string} head
//  * @return {number}
//  */
//
// function getIntegerLength(head) {
//   if (head >= "a" && head <= "z") {
//     return head.charCodeAt(0) - "a".charCodeAt(0) + 2;
//   } else if (head >= "A" && head <= "Z") {
//     return "Z".charCodeAt(0) - head.charCodeAt(0) + 2;
//   } else {
//     throw new Error("invalid order key head: " + head);
//   }
// }
//
// /**
//  * @param {string} key
//  * @return {string}
//  */
//
// function getIntegerPart(key) {
//   const integerPartLength = getIntegerLength(key[0]);
//   if (integerPartLength > key.length) {
//     throw new Error("invalid order key: " + key);
//   }
//   return key.slice(0, integerPartLength);
// }
//
// /**
//  * @param {string} key
//  * @param {string} digits
//  * @return {void}
//  */
//
// function validateOrderKey(key, digits) {
//   if (key === "A" + digits[0].repeat(26)) {
//     throw new Error("invalid order key: " + key);
//   }
//   // getIntegerPart will throw if the first character is bad,
//   // or the key is too short.  we'd call it to check these things
//   // even if we didn't need the result
//   const i = getIntegerPart(key);
//   const f = key.slice(i.length);
//   if (f.slice(-1) === digits[0]) {
//     throw new Error("invalid order key: " + key);
//   }
// }
//
// // note that this may return null, as there is a largest integer
// /**
//  * @param {string} x
//  * @param {string} digits
//  * @return {string | null}
//  */
// function incrementInteger(x, digits) {
//   validateInteger(x);
//   const [head, ...digs] = x.split("");
//   let carry = true;
//   for (let i = digs.length - 1; carry && i >= 0; i--) {
//     const d = digits.indexOf(digs[i]) + 1;
//     if (d === digits.length) {
//       digs[i] = digits[0];
//     } else {
//       digs[i] = digits[d];
//       carry = false;
//     }
//   }
//   if (carry) {
//     if (head === "Z") {
//       return "a" + digits[0];
//     }
//     if (head === "z") {
//       return null;
//     }
//     const h = String.fromCharCode(head.charCodeAt(0) + 1);
//     if (h > "a") {
//       digs.push(digits[0]);
//     } else {
//       digs.pop();
//     }
//     return h + digs.join("");
//   } else {
//     return head + digs.join("");
//   }
// }
//
// // note that this may return null, as there is a smallest integer
// /**
//  * @param {string} x
//  * @param {string} digits
//  * @return {string | null}
//  */
//
// function decrementInteger(x, digits) {
//   validateInteger(x);
//   const [head, ...digs] = x.split("");
//   let borrow = true;
//   for (let i = digs.length - 1; borrow && i >= 0; i--) {
//     const d = digits.indexOf(digs[i]) - 1;
//     if (d === -1) {
//       digs[i] = digits.slice(-1);
//     } else {
//       digs[i] = digits[d];
//       borrow = false;
//     }
//   }
//   if (borrow) {
//     if (head === "a") {
//       return "Z" + digits.slice(-1);
//     }
//     if (head === "A") {
//       return null;
//     }
//     const h = String.fromCharCode(head.charCodeAt(0) - 1);
//     if (h < "Z") {
//       digs.push(digits.slice(-1));
//     } else {
//       digs.pop();
//     }
//     return h + digs.join("");
//   } else {
//     return head + digs.join("");
//   }
// }
//
// // `a` is an order key or null (START).
// // `b` is an order key or null (END).
// // `a < b` lexicographically if both are non-null.
// // digits is a string such as '0123456789' for base 10.  Digits must be in
// // ascending character code order!
// /**
//  * @param {string | null | undefined} a
//  * @param {string | null | undefined} b
//  * @param {string=} digits
//  * @return {string}
//  */
// export function generateKeyBetween(a, b, digits = BASE_62_DIGITS) {
//   if (a != null) {
//     validateOrderKey(a, digits);
//   }
//   if (b != null) {
//     validateOrderKey(b, digits);
//   }
//   if (a != null && b != null && a >= b) {
//     throw new Error(a + " >= " + b);
//   }
//   if (a == null) {
//     if (b == null) {
//       return "a" + digits[0];
//     }
//
//     const ib = getIntegerPart(b);
//     const fb = b.slice(ib.length);
//     if (ib === "A" + digits[0].repeat(26)) {
//       return ib + midpoint("", fb, digits);
//     }
//     if (ib < b) {
//       return ib;
//     }
//     const res = decrementInteger(ib, digits);
//     if (res == null) {
//       throw new Error("cannot decrement any more");
//     }
//     return res;
//   }
//
//   if (b == null) {
//     const ia = getIntegerPart(a);
//     const fa = a.slice(ia.length);
//     const i = incrementInteger(ia, digits);
//     return i == null ? ia + midpoint(fa, null, digits) : i;
//   }
//
//   const ia = getIntegerPart(a);
//   const fa = a.slice(ia.length);
//   const ib = getIntegerPart(b);
//   const fb = b.slice(ib.length);
//   if (ia === ib) {
//     return ia + midpoint(fa, fb, digits);
//   }
//   const i = incrementInteger(ia, digits);
//   if (i == null) {
//     throw new Error("cannot increment any more");
//   }
//   if (i < b) {
//     return i;
//   }
//   return ia + midpoint(fa, null, digits);
// }
//
// /**
//  * same preconditions as generateKeysBetween.
//  * n >= 0.
//  * Returns an array of n distinct keys in sorted order.
//  * If a and b are both null, returns [a0, a1, ...]
//  * If one or the other is null, returns consecutive "integer"
//  * keys.  Otherwise, returns relatively short keys between
//  * a and b.
//  * @param {string | null | undefined} a
//  * @param {string | null | undefined} b
//  * @param {number} n
//  * @param {string} digits
//  * @return {string[]}
//  */
// export function generateNKeysBetween(a, b, n, digits = BASE_62_DIGITS) {
//   if (n === 0) {
//     return [];
//   }
//   if (n === 1) {
//     return [generateKeyBetween(a, b, digits)];
//   }
//   if (b == null) {
//     let c = generateKeyBetween(a, b, digits);
//     const result = [c];
//     for (let i = 0; i < n - 1; i++) {
//       c = generateKeyBetween(c, b, digits);
//       result.push(c);
//     }
//     return result;
//   }
//   if (a == null) {
//     let c = generateKeyBetween(a, b, digits);
//     const result = [c];
//     for (let i = 0; i < n - 1; i++) {
//       c = generateKeyBetween(a, c, digits);
//       result.push(c);
//     }
//     result.reverse();
//     return result;
//   }
//   const mid = Math.floor(n / 2);
//   const c = generateKeyBetween(a, b, digits);
//   return [
//     ...generateNKeysBetween(a, c, mid, digits),
//     c,
//     ...generateNKeysBetween(c, b, n - mid - 1, digits),
//   ];
// }
