package side.pxe.dhcp;

import io.netty.channel.socket.DatagramPacket;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import reactor.core.publisher.Mono;
import reactor.netty.udp.UdpServer;
import side.pxe.FixLogging;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ReferenceUdpEchoServer {
    void main() {
        FixLogging.fixLogging();

        var config = new Config();
        var udpServer = UdpServer.create()
                .host(config.listen.address)
                .port(config.listen.port)
                .handle((in, out) -> {
                    log.trace("handle");
                    return out.sendObject(in.receiveObject().map(o -> {
                        log.trace("got object: {}", o.getClass().getSimpleName());
                        if (o instanceof DatagramPacket p) {
                            var charSequence = p.content().getCharSequence(0,
                                    p.content().readableBytes(),
                                    StandardCharsets.UTF_8);
                            log.debug("incoming DatagramPacket content: '{}'", charSequence);

                            InetSocketAddress sender = p.sender();
                            log.debug("sender port: {}", sender.getPort());
                            log.debug("sender address: {}", sender.getAddress());
                            return new DatagramPacket(p.content().retain(), sender);
                        } else {
                            return Mono.error(new Exception("Unexpected type of the message: " + o));
                        }
                    }));
                });

        udpServer.warmup().block();
        var server = udpServer.bindNow();
        log.info("server listening on address: {}; port = '{}'",
                server.address(),
                server.address() instanceof InetSocketAddress i ? i.getPort() : null);
        server.onDispose().block();
    }

    @Data
    @Accessors(chain = true)
    static class Config {
        Listen listen = new Listen();

        @Data
        @Accessors(chain = true)
        static class Listen {
            @NonNull
            Type type = Type.LOCALHOST;
            String address = "127.0.0.1";
            @NonNull
            Integer port = 9000;

            @SuppressWarnings("unused")
            public String getAddress() {
                return switch (type) {
                    case LOCALHOST -> "127.0.0.1";
                    case ALL_INTERFACES -> "0.0.0.0";
                    case ALL_INTERFACES_IPV4_IPV6 -> "::";
                    case ADDRESS -> address;
                };
            }

            public enum Type {
                LOCALHOST,
                ALL_INTERFACES,
                ALL_INTERFACES_IPV4_IPV6,
                ADDRESS
            }
        }
    }
}
