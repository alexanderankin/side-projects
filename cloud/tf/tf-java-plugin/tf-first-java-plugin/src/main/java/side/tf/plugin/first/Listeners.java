package side.tf.plugin.first;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

// ok so these are going to have to do grpc stuff anyway, oops
public interface Listeners {
    @Slf4j
    class TcpSocketListener {
        private static final int PORT = 8080;
        private static final int BUFFER_SIZE = 256;
        Integer minPort;
        Integer maxPort;
        boolean listening;
        int listeningPort;
        Selector selector;
        ServerSocketChannel serverChannel;

        @SneakyThrows
        private static void handleAccept(ServerSocketChannel serverChannel, Selector selector) {
            // Accept the incoming client connection
            SocketChannel clientChannel = serverChannel.accept();
            if (clientChannel != null) {
                // Configure the client connection as non-blocking
                clientChannel.configureBlocking(false);

                // Register this client channel for READ operations
                clientChannel.register(selector, SelectionKey.OP_READ);
                System.out.println("Accepted new connection from: " + clientChannel.getRemoteAddress());
            }
        }

        @SneakyThrows
        private static void handleRead(SelectionKey key) {
            SocketChannel clientChannel = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

            int bytesRead = clientChannel.read(buffer);

            // Check for normal client disconnection signal (-1)
            if (bytesRead == -1) {
                System.out.println("Client disconnected cleanly: " + clientChannel.getRemoteAddress());
                key.cancel();
                clientChannel.close();
                return;
            }

            if (bytesRead > 0) {
                // Switch buffer from writing mode to reading mode
                buffer.flip();

                // Echo the received bytes straight back to the client channel
                while (buffer.hasRemaining()) {
                    clientChannel.write(buffer);
                }

                // Prepare buffer for the next round of reading operations
                buffer.clear();
            }
        }

        @SneakyThrows
        void listen() {
            if (listening) {
                log.info("already listening on port {}", listeningPort);
                return;
            }

            log.info("Starting NIO TCP Server on port range {}-{}", minPort, maxPort);

            selector = Selector.open();
            serverChannel = ServerSocketChannel.open();
            try {
                // 1. Check that the channel successfully opened, then configure non-blocking mode
                if (serverChannel.isOpen() && selector.isOpen()) {
                    serverChannel.configureBlocking(false);

                    // 2. Bind the socket to the local port
                    var minPort = Objects.requireNonNullElse(this.minPort, 30000);
                    var maxPort = Objects.requireNonNullElse(this.maxPort, 32767);
                    var bound = false;
                    for (int i = minPort; i < maxPort; i++) {
                        try {
                            serverChannel.bind(new InetSocketAddress(i));
                            bound = true;
                            break;
                        } catch (IOException ignored) {
                        }
                    }
                    if (!bound)
                        throw new IllegalStateException("could not bind on any port in range " + minPort + "-" + maxPort);

                    // 3. Register the channel to listen specifically for new connection events
                    serverChannel.register(selector, SelectionKey.OP_ACCEPT);

                    log.info("Server is running and listening for events...");

                    // 4. The main event loop
                    while (true) {
                        // This blocks until at least one registered network event occurs
                        selector.select();

                        // Retrieve the set of keys that have active events ready
                        Set<SelectionKey> selectedKeys = selector.selectedKeys();
                        Iterator<SelectionKey> iter = selectedKeys.iterator();

                        while (iter.hasNext()) {
                            SelectionKey key = iter.next();

                            // Always remove the key from the iterator to prevent processing it twice
                            iter.remove();

                            if (!key.isValid()) {
                                continue;
                            }

                            // 5. Handle the appropriate event type
                            if (key.isAcceptable()) {
                                handleAccept(serverChannel, selector);
                            } else if (key.isReadable()) {
                                handleRead(key);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Server exception: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    class UnixSocketListener {
        @SneakyThrows
        void listen() {
            // 1. Define the file path for the socket
            Path socketPath = Path.of(System.getProperty("user.home"), "myserver.socket");

            // 2. Ensure previous socket files are cleaned up before binding
            Files.deleteIfExists(socketPath);

            // 3. Create the Unix Domain Socket Address
            UnixDomainSocketAddress address = UnixDomainSocketAddress.of(socketPath);

            // 4. Open and bind the server channel using the UNIX protocol family
            try (ServerSocketChannel serverChannel = ServerSocketChannel.open(StandardProtocolFamily.UNIX)) {
                serverChannel.bind(address);
                System.out.println("[INFO] Server is listening on: " + socketPath);

                // 5. Accept incoming client connections
                while (true) {
                    try (SocketChannel clientChannel = serverChannel.accept()) {
                        System.out.println("[INFO] Client connected!");

                        // Allocate a buffer to read incoming data
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        int bytesRead = clientChannel.read(buffer);

                        if (bytesRead > 0) {
                            buffer.flip();
                            byte[] bytes = new byte[buffer.remaining()];
                            buffer.get(bytes);
                            String message = new String(bytes);
                            System.out.println("[RECEIVED] " + message.trim());
                        }
                    } catch (IOException e) {
                        System.err.println("[ERROR] Error handling client: " + e.getMessage());
                    }
                }
            } finally {
                // 6. Best practice: Clean up the file system when the server stops
                Files.deleteIfExists(socketPath);
            }
        }
    }
}
