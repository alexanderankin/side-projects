package org.example.webcam;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
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
import reactor.util.retry.Retry;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class QrServer implements Runnable {
    private final Config config;
    private final JsonMapper jsonMapper;
    private final QrService qrService;
    private final Scheduler uiScheduler = Schedulers.newSingle("qr-ui");
    private final Sinks.Many<String> input = Sinks.many().multicast().directBestEffort();
    private final Sinks.Many<UiCommand> uiCommands = Sinks.many().unicast().onBackpressureBuffer();
    private volatile JFrame frame;
    private volatile ImageIcon image;

    static void main() {
        log.info("starting QrServer");
        new QrServer(
                new Config()
                        .setQueueTimeout(Duration.ofSeconds(1))
                        .setMinOutputTime(Duration.ofSeconds(1))
                        .setFps(10),
                JsonMapper.builder().build(),
                new QrService(new QrService.Config()
                        .setSize(new Dimension(400, 400)))
        ).run();
    }

    @Override
    public void run() {
        log.info("starting things");
        uiCommands.asFlux()
                .publishOn(uiScheduler)
                .concatMap(cmd ->
                        runOnEdt(() -> showMessage(cmd.message))
                                .then(Mono.delay(config.getMinOutputTime()))
                                .doOnSuccess(ignored -> cmd.completion.success())
                                .doOnError(cmd.completion::error)
                )
                .doOnError(e -> log.error("error startUiPipeline", e))
                .retry()
                .subscribe();
        log.info("started ui pipeline");
        Flux.using(
                        this::openCameraStream,
                        this::decodeQrFlux,
                        CameraSession::close
                )
                .subscribeOn(Schedulers.boundedElastic())
                .doOnEach(s -> log.trace("startCapturePipeline signal: {}", s))
                .distinctUntilChanged()
                .doOnNext(System.out::println)
                .doOnNext(message -> input.emitNext(message, Sinks.EmitFailureHandler.busyLooping(config.getQueueTimeout())))
                .doOnError(e -> log.error("startCapturePipeline error", e))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(3)))
                .subscribe();
        log.info("started capture pipeline");

        HttpServer.create()
                .accessLog(true)
                .port(8080)
                .route(routes -> {
                    routes.get("/messages", (req, res) -> {
                        log.info("starting /messages sse stream");
                        return res.sse().send(inputFlux().map(this::toData), Objects::nonNull);
                    });

                    routes.post("/messages", (req, res) -> {
                        log.info("new /messages message");
                        return req.receive().aggregate().asString(StandardCharsets.UTF_8)
                                .flatMap(this::output)
                                .then(res.status(HttpResponseStatus.ACCEPTED).send());
                    });
                })
                .bind()
                .doOnSuccess(d -> log.info("started server on {}", d == null ? null : d.address()))
                .blockOptional().orElseThrow()
                .onDispose()
                .block();
    }

    private ByteBuf toData(String message) {
        try {
            var data = "data: " + jsonMapper.writeValueAsString(Map.of("message", message)) + "\n\n";
            return ByteBufAllocator.DEFAULT.buffer().writeBytes(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Failed to encode SSE payload", e);
        }
    }

    Flux<String> inputFlux() {
        return input.asFlux();
    }

    Mono<Void> output(String message) {
        return Mono.create(sink -> {
            uiCommands.emitNext(new UiCommand(message, sink), Sinks.EmitFailureHandler.busyLooping(config.getQueueTimeout()));
        });
    }

    @SneakyThrows
    private CameraSession openCameraStream() {
        enum Os {
            win, lin, mac;

            static Os current() {
                return Os.valueOf(System.getProperty("os.name").toLowerCase().substring(0, 3));
            }
        }
        Os current = Os.current();
        var config = Exec.Config.builder()
                .command("ffmpeg")
                .command("-f").command(switch (current) {
                    case win -> throw new UnsupportedOperationException();
                    case lin -> "v4l2";
                    case mac -> "avfoundation";
                })
                .command("-framerate").command("30")
                .command("-i").command(switch (current){
                    case win -> throw new UnsupportedOperationException();
                    case lin -> "/dev/video0";
                    case mac -> "0:none";
                })
                // .command("-f").command("matroska")
                // .command("-f").command("mjpeg")

                .command("-vf").command("fps=" + this.config.getFps() + ",scale=640:480")
                .command("-pix_fmt").command("bgr24")
                .command("-f").command("rawvideo")

                .command("-")
                .build();

        log.info("running ffmpeg with: {}", String.join(" ", config.getCommands()));

        var launched = Exec.INSTANCE.launch(config);
        InputStream inputStream = launched.result().getOut();

        if (launched.process().waitFor(Duration.ofSeconds(5))) {
            System.err.println("ffmpeg failed:");
            System.err.println("out: " + new String(launched.result().getOut().readAllBytes()));
            System.err.println("err: " + new String(launched.result().getErr().readAllBytes()));
            throw new Exec.ExitCodeException(config, launched.result());
        }

        try {
            // https://github.com/bytedeco/javacv/issues/1068
            var grabber = new FFmpegFrameGrabber(inputStream, 1_000_000);
            grabber.setFormat("rawvideo");
            grabber.setPixelFormat(avutil.AV_PIX_FMT_BGR24);
            // grabber.setPixelFormat(avutil.AV_PIX_FMT_UYVY422);
            grabber.setImageWidth(640);
            grabber.setImageHeight(480);
            // grabber.setFrameRate(this.config.getFps());
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
                    long t0 = System.nanoTime();
                    Frame frame = session.grabber.grabImage();
                    log.trace("decodeQrFlux frame: '{}' in {}", frame, Duration.ofNanos(System.nanoTime() - t0));
                    if (frame == null) {
                        continue;
                    }

                    BufferedImage image = session.converter.convert(frame);
                    log.trace("decodeQrFlux image: '{}'", image);
                    if (image == null) {
                        continue;
                    }

                    String decoded = qrService.decodeQR(image);
                    log.debug("decodeQrFlux decoded: '{}'", decoded);
                    if (decoded != null) {
                        sink.next(decoded);
                    }
                }
            } catch (Exception e) {
                sink.error(e);
            }
        });
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
            frame.getContentPane().setLayout(new FlowLayout());

            image = new ImageIcon(qrImage);
            frame.getContentPane().add(new JLabel(image));
            frame.pack();
            frame.setVisible(true);
        } else {
            image.setImage(qrImage);
            frame.repaint();
            frame.setVisible(true);
        }
    }

    private record UiCommand(String message, MonoSink<Void> completion) {
    }

    private record CameraSession(
            InputStream inputStream,
            FFmpegFrameGrabber grabber,
            Java2DFrameConverter converter
    ) implements AutoCloseable {
        @SneakyThrows
        @Override
        public void close() {
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

    @Data
    @Accessors(chain = true)
    public static class Config {
        Duration queueTimeout;
        Duration minOutputTime;
        int fps;
    }
}
