package info.ankin.projects.ovp4j.model.config;

import lombok.Data;
import lombok.experimental.Accessors;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

/**
 * @see <a href=https://openvpn.net/community-resources/reference-manual-for-openvpn-2-6/#options>doc</a>
 */
@Data
@Accessors(chain = true)
public class OpenVpnConfiguration {
    boolean client;
    Remote remote;
    Proto proto;
    Dev dev;

    public enum Proto {tcp, udp}

    public enum Dev {tun, tap}

    public enum Usr1Target {SIGHUP, SIGTERM}

    public enum ScriptSecurity {
        NO_EXTERNAL_PROGRAMS,

        /**
         * including: ifconfig, ip, route, netsh
         */
        ONLY_BUILTINS,

        /**
         * allow external programs
         */
        ALLOW,

        /**
         * allow external programs, and even passing secrets to them
         */
        ALLOW_PASSING_PASSWORDS,
    }

    /**
     * @see <a href=https://openvpn.net/community-resources/reference-manual-for-openvpn-2-6/#options:~:text=the%20traffic%20statistics.-,%2D%2Dstatus%2Dversion%C2%A0n,-Set%20the%20status>doc</a>
     */
    public enum StatusReportVersion {
        NONE, TRADITIONAL, V2, V2_TSV
    }

    public enum AllowedCompression {
        asym, no, yes
    }

    public enum KeyDirection {
        SERVER, CLIENT
    }

    public enum AuthRetry {
        /**
         * the default
         */
        none,

        /**
         * retry with the same credentials
         */
        @SuppressWarnings("SpellCheckingInspection")
        nointeract,

        /**
         * confirm the credentials before retrying
         */
        interact,
    }

    /**
     * for client and server
     */
    @Data
    @Accessors(chain = true)
    public static class GenericOptions {
        /**
         * do not cache user inputs for --askpass and --auth-user-pass
         */
        boolean authNoCache;

        /**
         * "change directory" - evaluate other file paths relative to this one
         */
        Path cd;

        // omitted for now
        // String compatVersion;

        boolean daemon;
        String daemonLogLabel;

        /**
         * "data channel offload" - for clients on protocol < 2.4
         */
        boolean disableDco;

        /**
         * see doc
         */
        boolean disableOcc;

        /**
         * whether to use hardware cryptography engine
         */
        boolean engine;

        /**
         * which engine to use
         *
         * @see #engine
         */
        String engineName;

        /**
         * improve udp performance - only on linux, without --shaper
         */
        boolean fastIo;

        /**
         * which user to run as after initialization
         *
         * @see #group
         */
        String user;

        /**
         * which group to run as after initialization
         *
         * @see #user
         */
        String group;

        /**
         * allow to ignore these options if they make parsing fail
         */
        List<String> ignoreUnknownOptions;

        /**
         * path to "ip" command (ip addr set...)
         */
        Path ipRoute;

        /**
         * @see <a href=https://www.rfc-editor.org/rfc/rfc5705.html>RFC5705</a>
         */
        KeyringMaterialExporter keyringMaterialExporter;

        /**
         * call mlock on linux to avoid writing sensitive data to disk
         */
        boolean mLock;

        /**
         * set linux process nice level
         */
        Integer nice;

        boolean persistKey;

        /**
         * openssl providers ('legacy', 'default', or path to shared object(?))
         */
        List<String> providers;

        /**
         * how to interpret USR 1 signals on linux
         */
        Usr1Target remapUsr1;

        /**
         * policy for calling external programs
         */
        ScriptSecurity scriptSecurity;

        /**
         * set {@code SELinux} context after initialization
         */
        String setCon;

        /**
         * configuration items regarding writing the status
         */
        StatusReportConfig status;

        /**
         * configure the status report version
         *
         * @see #status
         * @see StatusReportVersion
         */
        StatusReportVersion statusVersion;

        /**
         * whether a self test is requested or not
         */
        boolean testCrypto;

        Path tmpDir;

        boolean usePredictionResistance;

        /**
         * write a pid to this file
         */
        Path writePid;
    }

    @Data
    @Accessors(chain = true)
    public static class LogOptions {
        /**
         * strings, if starting with 'msg' logged to stdout, else logged to management server
         */
        List<String> echo;

        boolean errorsToStderr;

        Path log;

        boolean logAppend;

        /**
         * affects timestamps in logs
         */
        boolean machineReadableOutput;

        /**
         * silence repetitive logs after specified number of repetitions.
         */
        Integer mute;

        /**
         * common in wi-fi scenarios
         */
        boolean muteReplayWarnings;

        boolean suppressTimestamps;

        /**
         * label to use when logging to stdout.
         * like {@link GenericOptions#daemonLogLabel}, except for foreground.
         */
        String syslog;

        int verbosity = 1;
    }

    @Data
    @Accessors(chain = true)
    public static class ProtocolOptions {
        /**
         * compression = hazardous, off by default
         */
        AllowedCompression allowCompression = AllowedCompression.no;

        /**
         * go figure
         */
        String auth;

        /**
         * being removed - only used with PSK (also being removed)
         *
         * @see #dataCiphers
         */
        String cipher;

        /**
         * options: lzo, lz4, lz4-v2, stub, stub-v2, migrate
         * <p>
         * See e.g. the CRIME and BREACH attacks on TLS and vORACLE on VPNs
         * which also leverage to break encryption.
         */
        String compression;

        /**
         * there used to be a flag for this particular one
         */
        boolean compLzo;

        /**
         * @see #compLzo
         */
        boolean compNoAdapt;

        KeyDirection keyDirection = KeyDirection.SERVER;

        /**
         * colon-separated in config file,
         */
        List<String> dataCiphers;

        /**
         * to be removed, static (non-tls, from "genkey") channel encryption config
         */
        KeyEncryptionConfig secret;

        Duration transactionWindow;
    }

    @Data
    @Accessors(chain = true)
    public static class ClientOptions {
        /**
         * accept domain names (rather than just ip addresses) for
         * --ifconfig, --route, and --route-gateway
         */
        boolean allowPullFqdn;

        /**
         * do not drop tun packets with same destination as host (?).
         */
        boolean allowRecursiveRouting;

        /**
         * field to be used with dynamic server-side plugins (pushed to clients, not configured).
         */
        String authToken;

        /**
         * override the user field when password is set to an auth token.
         *
         * @see #authToken
         */
        String authTokenUser;

        /**
         * @see AuthUserPass
         */
        AuthUserPass authUserPass;

        AuthRetry authRetry;


    }

    @Data
    @Accessors(chain = true)
    public static class Remote {
        String host;
        int port;
    }

    @Data
    @Accessors(chain = true)
    public static class KeyringMaterialExporter {
        String label;

        /**
         * bytes, must be between 16 and 4095
         */
        int length;
    }

    @Data
    @Accessors(chain = true)
    public static class StatusReportConfig {
        /**
         * where to write the status to
         */
        Path file;

        /**
         * every how many seconds to write this file (since last USR2 signal).
         * <p>
         * has no effect if {@link #period} is specified.
         */
        int n = 60;

        /**
         * more sophisticated version of {@link #n}
         */
        Duration period;
    }

    @Data
    @Accessors(chain = true)
    public static class KeyEncryptionConfig {
        Path file;
        KeyDirection direction;
    }

    @Data
    @Accessors(chain = true)
    public static class AuthUserPass {
        /**
         * file with two lines - one with user and one with pass.
         * <p>
         * if omitted, should be interactively prompted.
         */
        Path userPass;
    }
}
