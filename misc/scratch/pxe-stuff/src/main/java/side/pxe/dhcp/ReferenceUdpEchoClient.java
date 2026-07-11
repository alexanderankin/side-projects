package side.pxe.dhcp;

import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.netty.Connection;
import reactor.netty.udp.UdpClient;
import side.pxe.FixLogging;

import java.net.PortUnreachableException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class ReferenceUdpEchoClient {
    static void main() {
        FixLogging.fixLogging();

        var client = new Client();
        System.out.print("prompt: ");
        var scanner = new Scanner(System.in);
        w:
        while (scanner.hasNextLine()) {
            var s = scanner.nextLine();
            if (!s.isBlank())
                try {
                    switch (s) {
                        case "h", "H" ->
                                System.out.println("H(help), D(disconnect), C(connect), R(reconnect), S(send), P(print), Q(quit)");
                        case "d", "D" -> client.disconnect();
                        case "c", "C" ->
                                client.connect(prompt(scanner, "host?"), Integer.parseInt(prompt(scanner, "port?")));
                        case "r", "R" -> client.reconnect();
                        case "s", "S" -> client.send(promptMessage(scanner));
                        case "p", "P" -> System.out.println(client);
                        case "q", "Q" -> {
                            log.info("quit");
                            break w;
                        }
                        default -> log.info("unrecognized");
                    }
                } catch (Exception e) {
                    System.out.println("error: " + e.getMessage());
                    log.debug("error in main loop", e);
                }

            System.out.print("prompt: ");
        }
    }

    static String prompt(Scanner s, String prompt) {
        System.out.print(prompt.trim() + ": ");
        return s.next();
    }

    static String promptMessage(Scanner s) {
        System.out.print("message?".trim() + ": ");
        return s.nextLine();
    }

    @Data
    @Accessors(chain = true)
    static class Client {
        UdpClient udpClientSpec;
        Connection connection;
        Sinks.Many<Object> sink;

        boolean canConnect() {
            return connection == null;
        }

        void mustConnect() {
            if (!canConnect())
                throw new IllegalStateException("disconnect before connecting");
        }

        void connect(String host, int port) {
            log.info("connect");
            mustConnect();
            udpClientSpec = UdpClient.create().host(host).port(port);
            reconnect();
        }

        void disconnect() {
            log.info("disconnect");
            if (canConnect())
                return;
            connection.disposeNow();
            connection = null;

        }

        void reconnect() {
            log.info("reconnect");
            if (udpClientSpec == null)
                throw new IllegalStateException("cannot reconnect - connect first");
            if (connection != null && connection.isDisposed())
                connection = null;
            mustConnect();
            connection = udpClientSpec.connectNow();
            sink = Sinks.many().multicast().directBestEffort();
            connection.inbound().receiveObject()
                    .onErrorResume(PortUnreachableException.class, _ -> {
                        log.debug("receiver disposing");
                        connection.dispose();
                        return Mono.empty();
                    })
                    .doOnError(e -> log.error("unknown error from receiveObject", e))
                    .doOnNext(sink::tryEmitNext)
                    .doOnComplete(sink::tryEmitComplete)
                    .subscribe();
        }

        @SneakyThrows
        void send(String message) {
            if (connection == null)
                throw new IllegalStateException("connect before sending");

            log.debug("sending message '{}'", message);
            if (connection.isDisposed()) {
                log.trace("connection is disposed, reconnecting...");
                reconnect();
            }

            var f = sink.asFlux().next()
                    .timeout(Duration.ofSeconds(5)).onErrorResume(TimeoutException.class, _ -> {
                        log.warn("reply timeout!");
                        return Mono.empty();
                    })
                    .doOnError(e -> log.info("reply error", e))
                    .doFinally(_ -> log.trace("reply finally"))
                    .doOnNext(n -> {
                        log.debug("reply next: {}", n);
                        if (!(n instanceof DatagramPacket d))
                            throw new IllegalStateException();
                        try {
                            var charSequence = d.content().getCharSequence(0, d.content().readableBytes(), StandardCharsets.UTF_8);
                            System.out.println(charSequence);
                        } catch (Exception e) {
                            throw new IllegalArgumentException("could not read reply string", e);
                        }
                    })
                    .toFuture();
            log.trace("started thread");
            connection.outbound()
                    .send(Mono.just(Unpooled.copiedBuffer(message, StandardCharsets.UTF_8)))
                    .then()
                    .timeout(Duration.ofSeconds(5)).onErrorResume(TimeoutException.class, _ -> {
                        log.warn("sender timeout!");
                        return Mono.empty();
                    })
                    .onErrorResume(PortUnreachableException.class, _ -> {
                        log.debug("sender disposing");
                        connection.dispose();
                        return Mono.empty();
                    })
                    .doOnError(e -> log.error("unknown error from sender", e))
                    .doFinally(_ -> log.trace("sender finally"))
                    .toFuture()
                    .get(5, TimeUnit.SECONDS);
            log.trace("sent");
            f.join();
        }
    }
}
