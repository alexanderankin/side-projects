package side.notes.backend.service;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * this is code from <a href=https://github.com/rocicorp/fractional-indexing>rocicorp/fractional-indexing</a>
 */
@Service
public class FractionalIndexService {
    private static final byte[] BASE_62_DIGITS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".getBytes();

    /**
     * @param a      may be empty string
     * @param b      is null or non-empty string (`a < b` lexicographically if b is not null)
     * @param digits characters in ascending character order
     */
    public String midpoint(@NonNull String a, @Nullable String b, byte @NonNull [] digits) {
        var zero = digits[0];
        int aCompareToB;
        if (b != null && (aCompareToB = a.compareTo(b)) >= 0) {
            throw new IllegalArgumentException("a '" + a + "' cannot be greater than b '" + b + "', a.compareTo(b) returned: " + aCompareToB);
        }
        if (a.charAt(a.length() - 1) == zero || (b != null && b.charAt(b.length() - 1) == zero)) {
            throw new IllegalArgumentException("neither a '" + a + "' or b '" + b + "' can have trailing zeroes ('" + zero + "')");
        }
        var aBytes = a.getBytes();
        var bBytes = b == null ? null : b.getBytes();

        if (b != null) {
            int n = 0;
            while ((aBytes[n] | zero) == bBytes[n]) {
                n++;
            }
            if (n > 0) {
                return b.substring(0, n) + midpoint(a.substring(n), b.substring(n), digits);
            }
        }

        var aDigit = aBytes.length > 0 ? Arrays.binarySearch(digits, aBytes[0]) : 0;
        var bDigit = bBytes != null ? Arrays.binarySearch(digits, bBytes[0]) : 0;
        if (bDigit - aDigit > 1) {
            var midDigit = round(0.5 * (aDigit + bDigit));
            return String.valueOf((char) digits[midDigit]);
        } else {
            if (b != null && bBytes.length > 1) {
                return new String(Arrays.copyOfRange(bBytes, 0, 1));
            } else {
                return (char) digits[aDigit] + midpoint(a.substring(1), null, digits);
            }
        }
    }

    int round(double input) {
        return Math.round((float) input);
    }

    public void validateInteger(String integer) {
        if (integer.length() != getIntegerLength(integer.charAt(0))) {
            throw new IllegalArgumentException("invalid integer part of order key: " + integer);
        }
    }

    public int getIntegerLength(char head) {
        if (head >= 'a' && head <= 'z') {
            return head - 'a' + 2;
        } else if (head >= 'A' && head <= 'Z') {
            return 'Z' - head + 2;
        }

        throw new IllegalArgumentException("invalid order key head: " + head);
    }

    public String getIntegerPart(String key) {
        var integerPartLength = getIntegerLength(key.charAt(0));
        if (integerPartLength > key.length()) {
            throw new IllegalArgumentException("invalid order key: " + key);
        }
        return key.substring(0, integerPartLength);
    }

    void validateOrderKey(String key, String digits) {
        if (key.equals("A" + repeatTwentySixTimes(digits.charAt(0)))) {
            throw new IllegalArgumentException("invalid order key: " + key);
        }
        var i = getIntegerPart(key); // throws if first char bad
        var f = key.substring(i.length());
        if (f.substring(f.length() - 2).equals(digits.substring(0, 1))) {
            throw new IllegalArgumentException("invalid order key: " + key);
        }
    }

    private String repeatTwentySixTimes(char c) {
        var sb = new StringBuilder();
        for (var i = 0; i < 26; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    private String incrementInteger(String x, String digits) {
        validateInteger(x);

        var digitsBytes = digits.getBytes();

        var xBytes = x.getBytes();
        var head = xBytes[0];
        var digs = Arrays.copyOfRange(xBytes, 1, xBytes.length);
        var carry = true;
        for (var i = digs.length - 1; i >= 0; i--) {
            var d = Arrays.binarySearch(digitsBytes, digs[i]) + 1;
            if (d == digits.length()) {
                digs[i] = digitsBytes[0];
            } else {
                digs[i] = digitsBytes[d];
                carry = false;
            }
        }
        if (carry) {
            if (head == 'Z') {
                return "a" + digitsBytes[0];
            }
            if (head == 'z') {
                return null;
            }
            var h = String.valueOf(head);
            if (h.compareTo("a") > 0) {
                digs = Arrays.copyOf(digs, digs.length + 1);
                digs[digs.length - 1] = digitsBytes[0];
            } else {
                digs = Arrays.copyOf(digs, digs.length - 1);
            }
            return h + new String(digs);
        }
        return head + new String(digs);
    }

    @Nullable
    private String decrementInteger(String x, String digits) {
        validateInteger(x);

        var digitsBytes = digits.getBytes();
        var xBytes = x.getBytes();

        char head = (char) xBytes[0];
        byte[] digs = Arrays.copyOfRange(xBytes, 1, xBytes.length);

        boolean borrow = true;
        for (int i = digs.length - 1; i >= 0 && borrow; i--) {
            int d = Arrays.binarySearch(digitsBytes, digs[i]) - 1;
            if (d == -1) {
                digs[i] = digitsBytes[digitsBytes.length - 1];
            } else {
                digs[i] = digitsBytes[d];
                borrow = false;
            }
        }

        if (borrow) {
            if (head == 'a') {
                return "Z" + (char) digitsBytes[digitsBytes.length - 1];
            }
            if (head == 'A') {
                return null;
            }

            char h = (char) (head - 1);
            if (h < 'Z') {
                digs = Arrays.copyOf(digs, digs.length + 1);
                digs[digs.length - 1] = digitsBytes[digitsBytes.length - 1];
            } else {
                digs = Arrays.copyOf(digs, digs.length - 1);
            }
            return h + new String(digs);
        }

        return head + new String(digs);
    }

    public String generateKeyBetween(@Nullable String a, @Nullable String b, String digits) {
        if (a != null) validateOrderKey(a, digits);
        if (b != null) validateOrderKey(b, digits);

        if (a != null && b != null && a.compareTo(b) >= 0) {
            throw new IllegalArgumentException(a + " >= " + b);
        }

        if (a == null) {
            if (b == null) {
                return "a" + digits.charAt(0);
            }

            String ib = getIntegerPart(b);
            String fb = b.substring(ib.length());

            if (ib.equals("A" + repeatTwentySixTimes(digits.charAt(0)))) {
                return ib + midpoint("", fb, digits.getBytes());
            }

            if (ib.compareTo(b) < 0) {
                return ib;
            }

            String res = decrementInteger(ib, digits);
            if (res == null) {
                throw new IllegalArgumentException("cannot decrement any more");
            }
            return res;
        }

        if (b == null) {
            String ia = getIntegerPart(a);
            String fa = a.substring(ia.length());

            String i = incrementInteger(ia, digits);
            return i == null ? ia + midpoint(fa, null, digits.getBytes()) : i;
        }

        String ia = getIntegerPart(a);
        String fa = a.substring(ia.length());
        String ib = getIntegerPart(b);
        String fb = b.substring(ib.length());

        if (ia.equals(ib)) {
            return ia + midpoint(fa, fb, digits.getBytes());
        }

        String i = incrementInteger(ia, digits);
        if (i == null) {
            throw new IllegalArgumentException("cannot increment any more");
        }

        if (i.compareTo(b) < 0) {
            return i;
        }

        return ia + midpoint(fa, null, digits.getBytes());
    }

    public String[] generateNKeysBetween(@Nullable String a, @Nullable String b, int n, String digits) {
        if (n == 0) return new String[0];

        if (n == 1) {
            return new String[]{generateKeyBetween(a, b, digits)};
        }

        if (b == null) {
            String[] result = new String[n];
            String c = generateKeyBetween(a, b, digits);
            result[0] = c;

            for (int i = 1; i < n; i++) {
                c = generateKeyBetween(c, b, digits);
                result[i] = c;
            }
            return result;
        }

        if (a == null) {
            String[] result = new String[n];
            String c = generateKeyBetween(a, b, digits);
            result[0] = c;

            for (int i = 1; i < n; i++) {
                c = generateKeyBetween(a, c, digits);
                result[i] = c;
            }

            // reverse
            for (int i = 0; i < n / 2; i++) {
                String tmp = result[i];
                result[i] = result[n - 1 - i];
                result[n - 1 - i] = tmp;
            }

            return result;
        }

        int mid = n / 2;
        String c = generateKeyBetween(a, b, digits);

        String[] left = generateNKeysBetween(a, c, mid, digits);
        String[] right = generateNKeysBetween(c, b, n - mid - 1, digits);

        String[] result = new String[n];
        System.arraycopy(left, 0, result, 0, left.length);
        result[left.length] = c;
        System.arraycopy(right, 0, result, left.length + 1, right.length);

        return result;
    }

    public String generateKeyBetween(String a, String b) {
        return generateKeyBetween(a, b, new String(BASE_62_DIGITS));
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
