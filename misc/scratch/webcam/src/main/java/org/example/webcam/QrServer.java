package org.example.webcam;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
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

@Slf4j
public class QrServer {

    public static void main(String[] args) {
        log.info("starting QrServer");
        var thing = new Thing(new QrService(new QrService.Config().setSize(new Dimension(400, 400))));
        log.info("starting thing");
        thing.start();

        HttpServer.create()
                .accessLog(true)
                .port(8080)
                .route(routes -> {
                    routes.get("/messages", (req, res) -> {
                        log.info("starting /messages sse stream");
                        return res.sse().send(thing.inputFlux().map(QrServer::toByteBuf), Objects::nonNull);
                    });

                    routes.post("/messages", (req, res) -> {
                        log.info("new /messages message");
                        return req.receive().aggregate().asString(StandardCharsets.UTF_8)
                                .flatMap(thing::show)
                                .then(res.status(HttpResponseStatus.ACCEPTED).send());
                    });
                })
                .bindNow()
                .onDispose()
                .block();
    }

    private static ByteBuf toByteBuf(String message) {
        try {
            var charset = StandardCharsets.UTF_8;
            var out = new ByteArrayOutputStream();
            out.write("data: ".getBytes(charset));
            out.write(("{\"message\":\"" + message + "\"}").getBytes(charset));
            out.write("\n\n".getBytes(charset));
            return ByteBufAllocator.DEFAULT.buffer().writeBytes(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to encode SSE payload", e);
        }
    }

    static final class Thing {
        private final QrService qrService;

        private final Scheduler uiScheduler = Schedulers.newSingle("qr-ui");
        private final Sinks.Many<String> input =
                Sinks.many().multicast().onBackpressureBuffer();

        private final Sinks.Many<UiCommand> uiCommands =
                Sinks.many().unicast().onBackpressureBuffer();

        private volatile JFrame frame;
        private volatile ImageIcon image;

        Thing(QrService qrService) {
            this.qrService = qrService;
        }

        void start() {
            startUiPipeline();
            log.info("started ui pipeline");
            startCapturePipeline();
            log.info("started capture pipeline");
        }

        Flux<String> inputFlux() {
            return input.asFlux();
        }

        Mono<Void> show(String message) {
            return Mono.create(sink -> {
                uiCommands.emitNext(new UiCommand(message, sink), Sinks.EmitFailureHandler.busyLooping(Duration.ofSeconds(1)));
            });
        }

        private void startUiPipeline() {
            uiCommands.asFlux()
                    .publishOn(uiScheduler)
                    .concatMap(cmd ->
                            runOnEdt(() -> showMessage(cmd.message))
                                    .then(Mono.delay(Duration.ofSeconds(1)))
                                    .doOnSuccess(ignored -> cmd.completion.success())
                                    .doOnError(cmd.completion::error)
                    )
                    .doOnError(e -> log.error("error startUiPipeline", e))
                    .retry()
                    .subscribe();
        }

        private void startCapturePipeline() {
            Flux.using(
                            this::openCameraStream,
                            this::decodeQrFlux,
                            this::closeCameraStream
                    )
                    .subscribeOn(Schedulers.boundedElastic())
                    .doOnEach(s -> log.info("startCapturePipeline signal: {}", s))
                    .filter(Objects::nonNull)
                    .distinctUntilChanged()
                    .doOnNext(System.out::println)
                    .doOnNext(this::emitInput)
                    .doOnError(e -> log.error("startCapturePipeline", e))
                    .retry()
                    .subscribe();
        }

        private CameraSession openCameraStream() {
            var config = Exec.Config.builder()
                    .command("ffmpeg")
                    .command("-f").command("avfoundation")
                    .command("-framerate").command("30")
                    .command("-i").command("0:none")
                    // .command("-f").command("matroska")
                    // .command("-f").command("mjpeg")
                    .command("-f").command("rawvideo")
                    .command("-pix_fmt").command("bgr24")
                    .command("-video_size").command("1280x720")
                    .command("-")
                    .build();

            var launched = Exec.INSTANCE.launch(config);
            InputStream inputStream = launched.result().getOut();

            try {
                var grabber = new FFmpegFrameGrabber(inputStream);
                grabber.setFormat("rawvideo");
                grabber.setPixelFormat(avutil.AV_PIX_FMT_BGR24);
                grabber.setImageWidth(1280);
                grabber.setImageHeight(720);
                grabber.setFrameRate(30);
                log.info("grabber starting");
                grabber.start(false);
                log.info("grabber started");
                var cameraSession = new CameraSession(inputStream, grabber, new Java2DFrameConverter());
                log.info("created cameraSession");
                return cameraSession;
            } catch (Exception e) {
                try {
                    inputStream.close();
                } catch (Exception ee) {
                    log.error("openCameraStream", ee);
                }
                throw new RuntimeException("Failed to start FFmpegFrameGrabber", e);
            }
        }

        private Flux<String> decodeQrFlux(CameraSession session) {
            return Flux.create(sink -> {
                try {
                    while (!sink.isCancelled()) {
                        Frame frame = session.grabber.grabImage();
                        log.info("decodeQrFlux frame: '{}'", frame);
                        if (frame == null) {
                            continue;
                        }

                        BufferedImage image = session.converter.convert(frame);
                        log.info("decodeQrFlux image: '{}'", image);
                        if (image == null) {
                            continue;
                        }

                        String decoded = qrService.decodeQR(image);
                        log.info("decodeQrFlux decoded: '{}'", decoded);
                        if (decoded != null) {
                            sink.next(decoded);
                        }
                    }
                } catch (Exception e) {
                    sink.error(e);
                }
            });

            // return Flux.generate(sink -> {
            //     try {
            //         log.info("grabbing image");
            //         Frame frame = session.grabber.grabImage();
            //         log.info("frame was: {}", frame);
            //         if (frame == null) {
            //             return;
            //         }
            //
            //         BufferedImage image = session.converter.convert(frame);
            //         log.info("image was {}", image);
            //         if (image == null) {
            //             return;
            //         }
            //
            //         String decoded = qrService.decodeQR(image);
            //         if (decoded == null) {
            //             log.info("decoded value was null");
            //             return;
            //         }
            //
            //         sink.next(decoded);
            //     } catch (Exception e) {
            //         log.error("generation failed", e);
            //         sink.error(e);
            //     }
            // });
        }

        private void closeCameraStream(CameraSession session) {
            try {
                session.close();
            } catch (Exception e) {
                log.error("closeCameraStream", e);
            }
        }

        private void emitInput(String message) {
            input.emitNext(message, Sinks.EmitFailureHandler.busyLooping(Duration.ofSeconds(1)));
        }

        private Mono<Void> runOnEdt(Runnable action) {
            return Mono.create(sink -> {
                SwingUtilities.invokeLater(() -> {
                    try {
                        action.run();
                        sink.success();
                    } catch (Throwable t) {
                        sink.error(t);
                    }
                });
            });
        }

        private void showMessage(String message) {
            var qrImage = qrService.toQrImage(message);

            if (frame == null) {
                frame = new JFrame("QR");
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
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

        private record UiCommand(String message, MonoSink<Void> completion) {
        }

        private record CameraSession(
                InputStream inputStream,
                FFmpegFrameGrabber grabber,
                Java2DFrameConverter converter
        ) implements AutoCloseable {
            @Override
            public void close() throws Exception {
                Exception first = null;

                try {
                    converter.close();
                } catch (Exception e) {
                    first = e;
                }

                try {
                    grabber.close();
                } catch (Exception e) {
                    if (first == null) first = e;
                }

                try {
                    inputStream.close();
                } catch (Exception e) {
                    if (first == null) first = e;
                }

                if (first != null) throw first;
            }
        }
    }
}
