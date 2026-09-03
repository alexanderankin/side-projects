package side.tf.plugin.first;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.Optional;

public class FirstTofuPlugin {
    static void main() {
        // OpenTofu starts the plugin with the ... environment variables set
        var pluginInitVars = PluginInitVars.fromEnv();
        // On Windows, the plugin will scan from PLUGIN_MIN_PORT ... and open a TCP listen socket
        // On all other systems, the plugin will create a Unix socket
        // The plugin chooses one of the PLUGIN_PROTOCOL_VERSIONS to support ... 5 or 6.
        var pluginStartInfo = PluginStartInfo.getDefaults(pluginInitVars);
        // The plugin writes the following line to the stdout, terminated by a newline:
        System.out.print(pluginStartInfo.toStartLine());
        // OpenTofu now sends GRPC requests to the plugin.
        // The plugin may write logs to stderr, which OpenTofu records as plugin logs.
    }

    @Data
    @Accessors(chain = true)
    static class PluginStartInfo {
        CoreProtocolVersion coreProtocolVersion = CoreProtocolVersion.V1;
        ProtocolVersion protocolVersion;
        SocketType socketType;
        String socketAddr;
        Protocol protocol;
        String serverCert;

        static PluginStartInfo getDefaults(PluginInitVars pluginInitVars) {
            var pluginStartInfo = new PluginStartInfo();
            pluginStartInfo.setProtocolVersion(ProtocolVersion.V6);

            enum Os {
                win, lin, mac;

                static Os current() {
                    return Os.valueOf(System.getProperty("os.name").toLowerCase().substring(0, 3));
                }
            }

            switch (Os.current()) {
                case win -> {
                    Integer minPort = pluginInitVars.getMinPort();
                    Integer maxPort = pluginInitVars.getMaxPort();

                }
                case lin, mac -> {}
            }


            return pluginStartInfo;
        }

        /**
         * The plugin writes the following line to the stdout, terminated by a newline:
         * {@code <CoreProtocolVersion>|<ProtocolVersion>|<SocketType>|<SocketAddr>|<Protocol>|<ServerCert>\n}
         */
        String toStartLine() {
            return coreProtocolVersion + "|" +
                protocolVersion + "|" +
                socketType + "|" +
                socketAddr + "|" +
                protocol + "|" +
                serverCert + "\n";
        }

        enum CoreProtocolVersion {
            V1,
            ;

            public String toString() {
                return name().substring(1);
            }
        }

        enum ProtocolVersion {
            V5,
            V6,
            ;

            public String toString() {
                return name().substring(1);
            }
        }

        enum SocketType { unix, tcp }

        enum Protocol { grpc }
    }

    /**
     * OpenTofu starts the plugin with the PLUGIN_MIN_PORT, PLUGIN_MAX_PORT,
     * PLUGIN_PROTOCOL_VERSIONS and the PLUGIN_CLIENT_CERT environment
     * variables set. While OpenTofu currently doesn't use them, your
     * implementation should also implement handling the
     * PLUGIN_UNIX_SOCKET_DIR, PLUGIN_UNIX_SOCKET_GROUP, and
     * PLUGIN_MULTIPLEX_GRPC environment variables.
     */
    @Data
    @Accessors(chain = true)
    static class PluginInitVars {
        Integer minPort;
        Integer maxPort;
        String protocolVersions;
        String clientCert;
        String unixSocketDir;
        String unixSocketGroup;
        boolean multiPlexGrpc;

        static PluginInitVars fromEnv() {
            return fromEnv(System.getenv());
        }

        static PluginInitVars fromEnv(Map<String, String> env) {
            return new PluginInitVars()
                .setMinPort(Optional.ofNullable(env.get(VarNames.MIN_PORT)).map(Integer::parseInt).orElse(null))
                .setMaxPort(Optional.ofNullable(env.get(VarNames.MAX_PORT)).map(Integer::parseInt).orElse(null))
                .setProtocolVersions(env.get(VarNames.PROTOCOL_VERSIONS))
                .setClientCert(env.get(VarNames.CLIENT_CERT))
                .setUnixSocketDir(env.get(VarNames.UNIX_SOCKET_DIR))
                .setUnixSocketGroup(env.get(VarNames.UNIX_SOCKET_GROUP))
                .setMultiPlexGrpc(env.containsKey(VarNames.MULTIPLEX_GRPC))
                ;
        }

        private interface VarNames {
            String MIN_PORT = "PLUGIN_MIN_PORT";
            String MAX_PORT = "PLUGIN_MAX_PORT";
            String PROTOCOL_VERSIONS = "PLUGIN_PROTOCOL_VERSIONS";
            String CLIENT_CERT = "PLUGIN_CLIENT_CERT";
            String UNIX_SOCKET_DIR = "PLUGIN_UNIX_SOCKET_DIR";
            String UNIX_SOCKET_GROUP = "PLUGIN_UNIX_SOCKET_GROUP";
            String MULTIPLEX_GRPC = "PLUGIN_MULTIPLEX_GRPC";
        }
    }
}
