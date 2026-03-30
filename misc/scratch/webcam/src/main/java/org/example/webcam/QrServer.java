package org.example.webcam;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.SneakyThrows;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.server.HttpServer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Stream;

public class QrServer {
    static void main() {
        var thing = new Thing(new QrService(new QrService.Config().setSize(new Dimension(400, 400))));
        HttpServer.create()
                .accessLog(true)
                .route(routes -> {
                    routes.get("/messages", (req, res) -> res.sse().send(thing.input.asFlux().map(QrServer::toByteBuf), Objects::nonNull));
                    routes.post("/messages", (req, res) -> req.receive().aggregate().flatMap(thing::show).then(res.status(HttpResponseStatus.ACCEPTED).send()));
                })
                .port(8080)
                .bindNow()
                .onDispose().block();
    }

    private static ByteBuf toByteBuf(String message) {
        var charset = StandardCharsets.UTF_8;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.write("data: ".getBytes(charset));
            out.write(("{\"message\":\"" + message + "\"}").getBytes(charset));
            out.write("\n\n".getBytes(charset));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ByteBufAllocator.DEFAULT.buffer().writeBytes(out.toByteArray());
    }

    static class Thing {
        final QrService qrService;
        final Sinks.Many<Mono<Void>> queue;
        final Sinks.Many<String> input;
        JFrame frame;
        ImageIcon image;
        String last;

        Thing(QrService qrService) {
            this.qrService = qrService;
            queue = Sinks.many().unicast().onBackpressureBuffer();
            queue.asFlux() //
                    .concatMap(Function.identity()) // strict ordering
                    .subscribeOn(Schedulers.fromExecutor(Executors.newVirtualThreadPerTaskExecutor())) //
                    .subscribe();
            input = Sinks.many().multicast().onBackpressureBuffer();
            Thread.ofPlatform().start(this::initInput);
        }

        @SneakyThrows
        private void initInput() {
            var config = Exec.Config.builder()
                    .command("ffmpeg")
                    .command("-f")
                    .command("avfoundation")
                    .command("-framerate")
                    .command("30")
                    .command("-i")
                    .command("0:none")
                    .command("-f").command("matroska") // ensures streamable output
                    .command("-")
                    .build();

            var result = Exec.INSTANCE.launch(config);
            try (var inputStream = result.result().getOut();
                 var imageStream = images(inputStream)) {
                var qrStream = imageStream.filter(Objects::nonNull)
                        // .peek(this::preview)
                        .map(qrService::decodeQR);

                qrStream
                        .peek(each -> {
                            System.out.println(each);
                        })
                        .filter(Objects::nonNull)
                        .distinct().forEach(d -> {
                            input.emitNext(d, Sinks.EmitFailureHandler.busyLooping(Duration.ofSeconds(1)));
                        });
            }
        }

        @SneakyThrows
        Stream<BufferedImage> images(InputStream inputStream) {
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputStream);
            grabber.start();
            Java2DFrameConverter converter = new Java2DFrameConverter();

            Stream<BufferedImage> imageStream = Stream.generate(() -> {
                try {
                    var frame = grabber.grabImage();
                    if (frame == null) return null;
                    return converter.convert(frame);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            var close = new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    grabber.close();
                }
            };
            return imageStream.onClose(close);
        }


        @SneakyThrows
        void processStream(InputStream in, Sinks.Many<String> sink) {
            try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(in)) {
                grabber.setFormat("rawvideo");
                grabber.setPixelFormat(avutil.AV_PIX_FMT_BGR24);

                System.out.println("grabber starting");
                grabber.start();
                System.out.println("grabber started");
                try (Java2DFrameConverter converter = new Java2DFrameConverter()) {
                    var qr = new QrService(new QrService.Config().setSize(new Dimension(400, 400)));

                    System.out.println("hi");
                    while (!Thread.currentThread().isInterrupted()) {
                        var frame = grabber.grabImage();
                        if (frame == null)
                            continue;
                        var bufferedImage = converter.convert(frame);

                        String decoded = qr.decodeQR(bufferedImage);
                        if (decoded != null && !decoded.equals(last)) {
                            sink.tryEmitNext(decoded);
                            last = decoded;
                        }
                    }
                }
            }
        }

        void showMessage(String message) {
            var qrImage = qrService.toQrImage(message);

            if (image == null) {
                frame = new JFrame();
                frame.setSize(400, 400);
                frame.getContentPane().setLayout(new FlowLayout());
                image = new ImageIcon(qrImage);
                frame.getContentPane().add(new JLabel(image));
                frame.pack();
                frame.setVisible(true);
            } else {
                image.setImage(qrImage);
                frame.repaint();
            }
        }

        Mono<Void> show(ByteBuf byteBuf) {
            String message = byteBuf.toString(StandardCharsets.UTF_8);
            var task = Mono.fromRunnable(() -> showMessage(message)).then(Mono.delay(Duration.ofSeconds(1))).then();
            var completion = Sinks.<Void>one();
            var wrapped = task.doOnTerminate(completion::tryEmitEmpty);

            Sinks.EmitResult result;
            do {
                result = queue.tryEmitNext(wrapped);
            } while (result == Sinks.EmitResult.FAIL_NON_SERIALIZED);

            if (result.isFailure()) {
                return Mono.error(new IllegalStateException("Failed to enqueue: " + result));
            }

            return completion.asMono();
        }
    }
}
