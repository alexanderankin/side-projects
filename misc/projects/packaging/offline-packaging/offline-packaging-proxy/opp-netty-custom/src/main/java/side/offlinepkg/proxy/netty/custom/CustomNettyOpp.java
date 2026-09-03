package side.offlinepkg.proxy.netty.custom;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.Deque;


public class CustomNettyOpp {
    private static final int LISTEN_PORT = 8080;

    static void main(String[] args) throws Exception {
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup workers = new NioEventLoopGroup();

        try {
            ServerBootstrap server = new ServerBootstrap();

            server.group(boss, workers)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast("http", new HttpServerCodec());
                        ch.pipeline().addLast("proxy", new ProxyHandler());
                    }
                });

            Channel channel = server.bind(LISTEN_PORT).sync().channel();
            System.out.println("Proxy listening on http://127.0.0.1:" + LISTEN_PORT);
            channel.closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            workers.shutdownGracefully();
        }
    }

    /*
     * Work out where an ordinary HTTP request should go.
     */
    private static Target targetFor(HttpRequest request) {
        String uriText = request.uri();

        /*
         * Standard proxy request:
         *
         * GET http://example.com:8081/foo?q=1 HTTP/1.1
         */
        if (uriText.startsWith("http://") ||
            uriText.startsWith("https://")) {

            URI uri = URI.create(uriText);

            String host = uri.getHost();

            if (host == null) {
                throw new IllegalArgumentException("No host");
            }

            int port = uri.getPort();

            if (port == -1) {
                port = "https".equalsIgnoreCase(uri.getScheme())
                    ? 443
                    : 80;
            }

            String path = uri.getRawPath();

            if (path == null || path.isEmpty()) {
                path = "/";
            }

            if (uri.getRawQuery() != null) {
                path += "?" + uri.getRawQuery();
            }

            return new Target(host, port, path);
        }

        /*
         * Also accept origin-form:
         *
         * GET /foo HTTP/1.1
         * Host: example.com
         */
        String hostHeader = request.headers().get(HttpHeaderNames.HOST);

        if (hostHeader == null) {
            throw new IllegalArgumentException("Missing Host");
        }

        Target host = hostAndPort(hostHeader, 80);

        return new Target(host.host, host.port, uriText);
    }

    /*
     * CONNECT request-target is normally:
     *
     * example.com:443
     */
    private static Target connectTarget(String authority) {
        return hostAndPort(authority, 443);
    }

    /*
     * Handles:
     *
     * example.com
     * example.com:8080
     * [2001:db8::1]
     * [2001:db8::1]:8080
     */
    private static Target hostAndPort(String authority, int defaultPort) {
        if (authority.startsWith("[")) {
            int end = authority.indexOf(']');

            if (end == -1) {
                throw new IllegalArgumentException("Bad IPv6 address");
            }

            String host = authority.substring(1, end);

            if (end + 1 == authority.length()) {
                return new Target(host, defaultPort, null);
            }

            if (authority.charAt(end + 1) != ':') {
                throw new IllegalArgumentException("Bad authority");
            }

            int port = Integer.parseInt(
                authority.substring(end + 2)
            );

            return new Target(host, port, null);
        }

        int colon = authority.lastIndexOf(':');

        if (colon >= 0) {
            String host = authority.substring(0, colon);
            int port = Integer.parseInt(
                authority.substring(colon + 1)
            );

            return new Target(host, port, null);
        }

        return new Target(authority, defaultPort, null);
    }

    private static void fail(
        ChannelHandlerContext ctx,
        HttpResponseStatus status) {

        FullHttpResponse response =
            new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                status
            );

        response.headers().set(
            HttpHeaderNames.CONTENT_LENGTH,
            0
        );

        ctx.writeAndFlush(response)
            .addListener(ChannelFutureListener.CLOSE);
    }

    private static void close(Channel channel) {
        if (channel != null && channel.isActive()) {
            channel.close();
        }
    }

    /*
     * Handles the client -> proxy side.
     *
     * For:
     *
     *   GET http://example.com/foo HTTP/1.1
     *
     * we open example.com:80 and forward an HTTP request.
     *
     * For:
     *
     *   CONNECT example.com:443 HTTP/1.1
     *
     * we open example.com:443, send "200 Connection Established",
     * remove HTTP parsing, and become a dumb TCP tunnel.
     */
    private static final class ProxyHandler extends ChannelInboundHandlerAdapter {
        /*
         * A few HTTP objects can arrive while the outbound TCP connection
         * is still being established. Keep them here temporarily.
         */
        private final Deque<Object> pending = new ArrayDeque<>();
        private Channel upstream;
        private boolean connecting;

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {

            if (upstream != null) {
                upstream.writeAndFlush(msg);
                return;
            }

            if (connecting) {
                pending.addLast(msg);
                return;
            }

            if (!(msg instanceof HttpRequest request)) {
                ReferenceCountUtil.release(msg);
                return;
            }

            if (request.method().equals(HttpMethod.CONNECT)) {
                handleConnect(ctx, request);
            } else {
                handleHttp(ctx, request);
            }
        }

        /*
         * Ordinary HTTP proxy request.
         */
        private void handleHttp(ChannelHandlerContext ctx, HttpRequest request) {
            Target target = targetFor(request);

            // ---- GOOD PLACE FOR YOUR RULES -----------------------
            //
            // if (!allowedHost(target.host)) { ... }
            // if (!allowedPort(target.port)) { ... }
            // if (!authenticated(request.headers())) { ... }
            //
            // -------------------------------------------------------

            connecting = true;

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(ctx.channel().eventLoop())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(
                            new HttpClientCodec()
                        );

                        ch.pipeline().addLast(
                            new HttpResponseRelay(ctx.channel())
                        );
                    }
                });

            bootstrap.connect(target.host, target.port)
                .addListener((ChannelFutureListener) future -> {
                    if (!future.isSuccess()) {
                        ReferenceCountUtil.release(request);
                        fail(ctx, HttpResponseStatus.BAD_GATEWAY);
                        return;
                    }

                    upstream = future.channel();
                    connecting = false;

                    /*
                     * Proxies receive absolute-form:
                     *
                     * GET http://example.com/a HTTP/1.1
                     *
                     * Origin servers normally receive origin-form:
                     *
                     * GET /a HTTP/1.1
                     */
                    request.setUri(target.path);

                    /*
                     * These headers are about the client<->proxy
                     * connection, not proxy<->origin.
                     */
                    request.headers().remove("Proxy-Authorization");
                    request.headers().remove("Proxy-Connection");

                    /*
                     * Deliberately make the minimal example use one
                     * request per TCP connection.
                     *
                     * Remove this later if you add proper persistent
                     * connection handling/pooling.
                     */
                    request.headers().set(
                        HttpHeaderNames.CONNECTION,
                        HttpHeaderValues.CLOSE
                    );

                    upstream.write(request);

                    while (!pending.isEmpty()) {
                        upstream.write(pending.removeFirst());
                    }

                    upstream.flush();
                });
        }

        /*
         * HTTPS CONNECT.
         *
         * No TLS code is required here.
         *
         * Browser:
         *
         *   CONNECT example.com:443 HTTP/1.1
         *
         * Proxy opens raw TCP connection to example.com:443.
         * After success:
         *
         *   HTTP/1.1 200 Connection Established
         *
         * From then onward all bytes are copied unchanged.
         */
        private void handleConnect(ChannelHandlerContext ctx, HttpRequest request) {

            Target target;

            try {
                target = connectTarget(request.uri());
            } catch (Exception e) {
                ReferenceCountUtil.release(request);
                fail(ctx, HttpResponseStatus.BAD_REQUEST);
                return;
            }

            // ---- ANOTHER GOOD PLACE FOR RULES --------------------
            //
            // Usually you especially want CONNECT port restrictions:
            //
            // if (target.port != 443) reject(...);
            //
            // Otherwise an open proxy can be abused to reach arbitrary
            // TCP services.
            //
            // -------------------------------------------------------

            connecting = true;

            Bootstrap bootstrap = new Bootstrap();

            bootstrap.group(ctx.channel().eventLoop())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        // Raw TCP. No HTTP codec.
                    }
                });

            bootstrap.connect(target.host, target.port)
                .addListener((ChannelFutureListener) future -> {

                    ReferenceCountUtil.release(request);

                    if (!future.isSuccess()) {
                        fail(ctx, HttpResponseStatus.BAD_GATEWAY);
                        return;
                    }

                    upstream = future.channel();

                    FullHttpResponse response =
                        new DefaultFullHttpResponse(
                            HttpVersion.HTTP_1_1,
                            new HttpResponseStatus(
                                200,
                                "Connection Established"
                            )
                        );

                    ctx.writeAndFlush(response)
                        .addListener(done -> {
                            if (!done.isSuccess()) {
                                close(upstream);
                                close(ctx.channel());
                                return;
                            }

                            /*
                             * The CONNECT response has now gone through
                             * HttpServerCodec.
                             *
                             * Remove HTTP parsing because everything
                             * after this point is TLS/raw TCP bytes.
                             */
                            ctx.pipeline().remove("http");
                            ctx.pipeline().remove("proxy");

                            ctx.pipeline().addLast(
                                new RawRelayHandler(upstream)
                            );

                            upstream.pipeline().addLast(
                                new RawRelayHandler(ctx.channel())
                            );

                            connecting = false;

                            /*
                             * Normally there won't be anything pending:
                             * CONNECT clients wait for the 200 before
                             * starting TLS.
                             */
                            while (!pending.isEmpty()) {
                                Object object = pending.removeFirst();

                                if (object instanceof ByteBuf bytes) {
                                    upstream.write(bytes);
                                } else {
                                    ReferenceCountUtil.release(object);
                                }
                            }

                            upstream.flush();
                        });
                });
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            close(upstream);
            releasePending();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();

            releasePending();
            close(upstream);
            close(ctx.channel());
        }

        private void releasePending() {
            while (!pending.isEmpty()) {
                ReferenceCountUtil.release(pending.removeFirst());
            }
        }
    }

    /*
     * Origin HTTP response -> proxy client.
     *
     * HttpClientCodec parses the response into:
     *
     *   HttpResponse
     *   HttpContent
     *   HttpContent
     *   LastHttpContent
     *
     * so the response body is streamed rather than accumulated into
     * one giant byte[].
     */
    @RequiredArgsConstructor
    private static final class HttpResponseRelay extends ChannelInboundHandlerAdapter {
        private final Channel client;

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            client.writeAndFlush(msg);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            close(client);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            close(ctx.channel());
            close(client);
        }
    }

    /*
     * Dumb TCP pipe used after CONNECT succeeds.
     *
     * At this level a TLS session is just bytes:
     *
     * client ---- encrypted bytes ----> server
     * client <--- encrypted bytes ----- server
     */
    @RequiredArgsConstructor
    private static final class RawRelayHandler extends ChannelInboundHandlerAdapter {
        private final Channel other;

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (other.isActive()) {
                other.writeAndFlush(msg);
            } else {
                ReferenceCountUtil.release(msg);
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            close(other);
        }

        @Override
        public void exceptionCaught(
            ChannelHandlerContext ctx,
            Throwable cause) {

            close(ctx.channel());
            close(other);
        }
    }

    private record Target(String host, int port, String path) {
    }
}
