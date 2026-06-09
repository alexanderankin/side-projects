package side.oauthcli;

import com.sun.jna.*;
import com.sun.jna.ptr.PointerByReference;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static side.oauthcli.KeyChainMacOsSupport.NativeLibraryHolder.CORE_FOUNDATION_LIBRARY;
import static side.oauthcli.KeyChainMacOsSupport.NativeLibraryHolder.SECURITY_LIBRARY;

final class KeyChainMacOsSupport {
    private static final int ERR_SEC_SUCCESS = 0;
    private static final int ERR_SEC_DUPLICATE_ITEM = -25299;
    private static final int ERR_SEC_ITEM_NOT_FOUND = -25300;
    private static final int kCFStringEncodingUTF8 = 0x08000100;
    private static final CoreFoundation CF = CoreFoundation.INSTANCE;
    private static final Security SEC = Security.INSTANCE;

    private KeyChainMacOsSupport() {
    }

    public static void main(String[] args) {
        String service = KeyChain.MacKeyChain.NAMESPACE;
        String account = "api-token";

        KeyChainMacOsSupport.saveText(service, account, "super secret text");

        String value = KeyChainMacOsSupport.readText(service, account).orElseThrow();

        System.out.println(value);

        KeyChainMacOsSupport.deleteText(service, account);

        KeyChainMacOsSupport.readText(service, account).ifPresent(ignored -> {
            throw new IllegalStateException();
        });
    }

    private static Pointer cfString(String value) {
        Pointer p = CF.CFStringCreateWithCString(null, value, kCFStringEncodingUTF8);
        if (p == null) {
            throw new IllegalStateException("CFStringCreateWithCString failed");
        }
        return p;
    }

    private static Pointer cfData(byte[] bytes) {
        Pointer p = CF.CFDataCreate(null, bytes, bytes.length);
        if (p == null) {
            throw new IllegalStateException("CFDataCreate failed");
        }
        return p;
    }

    /**
     * Security.framework constants like kSecClass are exported as CFStringRef globals.
     * In JNA, getGlobalVariableAddress(name) returns the address of the global variable,
     * so getPointer(0) dereferences it to the actual CFStringRef.
     */
    private static Pointer secConst(String name) {
        return SECURITY_LIBRARY.getGlobalVariableAddress(name).getPointer(0);
    }

    /**
     * CoreFoundation constants like kCFBooleanTrue are also exported as globals.
     */
    private static Pointer cfConst(String name) {
        return CORE_FOUNDATION_LIBRARY.getGlobalVariableAddress(name).getPointer(0);
    }

    private static String cfStringToJava(Pointer cfString) {
        if (cfString == null) return null;
        long length = CF.CFStringGetLength(cfString);
        long maxSize = CF.CFStringGetMaximumSizeForEncoding(length, kCFStringEncodingUTF8) + 1;
        try (Memory buffer = new Memory(maxSize)) {
            boolean ok = CF.CFStringGetCString(cfString, buffer, maxSize, kCFStringEncodingUTF8);
            if (!ok) throw new IllegalStateException("CFStringGetCString failed");
            return buffer.getString(0, StandardCharsets.UTF_8.name());
        }
    }

    private static Pointer mutableDictionary() {
        Pointer keyCallbacks = CORE_FOUNDATION_LIBRARY.getGlobalVariableAddress("kCFTypeDictionaryKeyCallBacks");
        Pointer valueCallbacks = CORE_FOUNDATION_LIBRARY.getGlobalVariableAddress("kCFTypeDictionaryValueCallBacks");

        Pointer dict = CF.CFDictionaryCreateMutable(null, 0, keyCallbacks, valueCallbacks);

        if (dict == null) {
            throw new IllegalStateException("CFDictionaryCreateMutable failed");
        }

        return dict;
    }

    private static Pointer baseQuery(String service, String account) {
        Pointer dict = mutableDictionary();

        Pointer serviceValue = cfString(service);
        Pointer accountValue = cfString(account);

        CF.CFDictionarySetValue(dict, secConst("kSecClass"), secConst("kSecClassGenericPassword"));
        CF.CFDictionarySetValue(dict, secConst("kSecAttrService"), serviceValue);
        CF.CFDictionarySetValue(dict, secConst("kSecAttrAccount"), accountValue);

        CF.CFRelease(serviceValue);
        CF.CFRelease(accountValue);

        return dict;
    }

    public static void saveText(String service, String account, String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);

        Pointer query = baseQuery(service, account);
        Pointer data = cfData(bytes);

        CF.CFDictionarySetValue(query, secConst("kSecValueData"), data);

        int status = SEC.SecItemAdd(query, null);

        CF.CFRelease(data);
        CF.CFRelease(query);

        if (status == ERR_SEC_DUPLICATE_ITEM) {
            updateText(service, account, text);
            return;
        }

        checkStatus(status, "SecItemAdd");
    }

    public static void updateText(String service, String account, String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);

        Pointer query = baseQuery(service, account);
        Pointer attrs = mutableDictionary();
        Pointer data = cfData(bytes);

        CF.CFDictionarySetValue(attrs, secConst("kSecValueData"), data);

        int status = SEC.SecItemUpdate(query, attrs);

        CF.CFRelease(data);
        CF.CFRelease(attrs);
        CF.CFRelease(query);

        checkStatus(status, "SecItemUpdate");
    }

    public static Optional<String> readText(String service, String account) {
        Pointer query = baseQuery(service, account);

        CF.CFDictionarySetValue(query, secConst("kSecReturnData"), cfConst("kCFBooleanTrue"));
        CF.CFDictionarySetValue(query, secConst("kSecMatchLimit"), secConst("kSecMatchLimitOne"));

        PointerByReference resultRef = new PointerByReference();

        int status = SEC.SecItemCopyMatching(query, resultRef);

        CF.CFRelease(query);

        if (status == ERR_SEC_ITEM_NOT_FOUND) {
            return Optional.empty();
        }

        checkStatus(status, "SecItemCopyMatching");

        Pointer data = resultRef.getValue();
        long length = CF.CFDataGetLength(data);
        Pointer bytePtr = CF.CFDataGetBytePtr(data);

        byte[] bytes = bytePtr.getByteArray(0, Math.toIntExact(length));

        CF.CFRelease(data);

        return Optional.of(new String(bytes, StandardCharsets.UTF_8));
    }

    public static boolean deleteText(String service, String account) {
        Pointer query = baseQuery(service, account);

        int status = SEC.SecItemDelete(query);
        CF.CFRelease(query);

        if (status == ERR_SEC_ITEM_NOT_FOUND) {
            return false;
        }

        checkStatus(status, "SecItemDelete");
        return true;
    }

    public static List<String> list(String account) {
        /*
         * Here, `account` is being used as the app/service namespace.
         *
         * Stored items look like:
         *
         *   kSecClass        = kSecClassGenericPassword
         *   kSecAttrService  = account
         *   kSecAttrAccount  = logical secret key
         *
         * This method returns all kSecAttrAccount values under that service.
         */

        Pointer query = mutableDictionary();
        Pointer serviceValue = cfString(account);
        CF.CFDictionarySetValue(query, secConst("kSecClass"), secConst("kSecClassGenericPassword"));
        CF.CFDictionarySetValue(query, secConst("kSecAttrService"), serviceValue);
        CF.CFDictionarySetValue(query, secConst("kSecReturnAttributes"), cfConst("kCFBooleanTrue"));
        CF.CFDictionarySetValue(query, secConst("kSecMatchLimit"), secConst("kSecMatchLimitAll"));

        PointerByReference resultRef = new PointerByReference();

        int status = SEC.SecItemCopyMatching(query, resultRef);
        CF.CFRelease(serviceValue);
        CF.CFRelease(query);

        if (status == ERR_SEC_ITEM_NOT_FOUND) {
            return List.of();
        }
        checkStatus(status, "SecItemCopyMatching");

        Pointer resultArray = resultRef.getValue();

        try {
            long count = CF.CFArrayGetCount(resultArray);
            List<String> names = new ArrayList<>(Math.toIntExact(count));
            Pointer accountKey = secConst("kSecAttrAccount");
            for (long i = 0; i < count; i++) {
                Pointer itemAttributes = CF.CFArrayGetValueAtIndex(resultArray, i);
                Pointer accountValue = CF.CFDictionaryGetValue(itemAttributes, accountKey);
                String name = cfStringToJava(accountValue);
                if (name != null) {
                    names.add(name);
                }
            }

            return names;
        } finally {
            CF.CFRelease(resultArray);
        }
    }

    private static void checkStatus(int status, String operation) {
        if (status != ERR_SEC_SUCCESS) {
            throw new IllegalStateException(operation + " failed with OSStatus " + status);
        }
    }

    private interface CoreFoundation extends Library {
        CoreFoundation INSTANCE = Native.load("/System/Library/Frameworks/CoreFoundation.framework/CoreFoundation", CoreFoundation.class);

        Pointer CFStringCreateWithCString(Pointer allocator, String cStr, int encoding);

        Pointer CFDataCreate(Pointer allocator, byte[] bytes, long length);

        long CFDataGetLength(Pointer data);

        Pointer CFDataGetBytePtr(Pointer data);

        Pointer CFDictionaryCreateMutable(Pointer allocator, long capacity, Pointer keyCallBacks, Pointer valueCallBacks);

        void CFDictionarySetValue(Pointer dictionary, Pointer key, Pointer value);

        void CFRelease(Pointer cfObject);

        //<editor-fold desc="for listing">
        long CFArrayGetCount(Pointer array);

        Pointer CFArrayGetValueAtIndex(Pointer array, long index);

        Pointer CFDictionaryGetValue(Pointer dictionary, Pointer key);

        long CFStringGetLength(Pointer string);

        long CFStringGetMaximumSizeForEncoding(long length, int encoding);

        boolean CFStringGetCString(Pointer string, Pointer buffer, long bufferSize, int encoding);
        //</editor-fold>
    }

    private interface Security extends Library {
        Security INSTANCE = Native.load("/System/Library/Frameworks/Security.framework/Security", Security.class);

        int SecItemAdd(Pointer attributes, PointerByReference result);

        int SecItemCopyMatching(Pointer query, PointerByReference result);

        int SecItemUpdate(Pointer query, Pointer attributesToUpdate);

        int SecItemDelete(Pointer query);
    }

    static final class NativeLibraryHolder {
        static final NativeLibrary SECURITY_LIBRARY =
                NativeLibrary.getInstance("/System/Library/Frameworks/Security.framework/Security");

        static final NativeLibrary CORE_FOUNDATION_LIBRARY =
                NativeLibrary.getInstance("/System/Library/Frameworks/CoreFoundation.framework/CoreFoundation");
    }
}
