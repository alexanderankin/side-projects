package org.example.webcam;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.regex.Pattern;

public class FfmpegExample {
    @SneakyThrows
    static void main() {
        var devices = new FfmpegDeviceLister().listDevices();
        System.out.println(devices);

        var stopper = new CountDownLatch(1);
        Thread.ofVirtual().start(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                Thread.sleep(10_000);
                stopper.countDown();
            }
        });
        new FfmpegDeviceCapture().capture(1, null, stopper);
    }

    public static class FfmpegDeviceCapture {
        @SuppressWarnings("Convert2Lambda")
        public void capture(Integer video, Integer audio, CountDownLatch stopper) {
            String input = (video != null ? video : "none") + ":" + (audio != null ? audio : "none");
            var config = Exec.Config.builder()
                    .command("ffmpeg")
                    .command("-f")
                    .command("avfoundation")
                    .command("-i")
                    .command(input)
                    .command("-f").command("matroska") // ensures streamable output
                    .command("-")
                    .build();

            var result = Exec.INSTANCE.launch(config);
            var byteCounter = new long[1];

            Thread.ofVirtual().start(() -> {
                try (var in = result.result().getOut()) {
                    byte[] buf = new byte[8192];
                    int r;
                    while ((r = in.read(buf)) != -1) {
                        byteCounter[0] += r;
                    }
                } catch (Exception e) {
                    if (e instanceof RuntimeException r) throw r;
                    throw new RuntimeException(e);
                }
            });

            Thread.ofVirtual().start(new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    result.result().getErr().transferTo(System.out);
                }
            });

            var stopperThread = Thread.ofVirtual().start(new Runnable() {
                @Override
                public void run() {
                    try {
                        stopper.await();
                    } catch (InterruptedException e) {
                        System.out.println("countdown timed out waiting to quit");
                        Thread.currentThread().interrupt();
                    }
                    System.out.println("countdown told us to quit");

                    // graceful-ish shutdown (SIGTERM)
                    result.process().destroy();

                    try {
                        if (!result.process().waitFor(3, java.util.concurrent.TimeUnit.SECONDS)) {
                            result.process().destroyForcibly();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                ;
            });

            var code = result.result().getCode();
            if (stopperThread.isAlive())
                stopperThread.interrupt();


            System.out.println("done with code: " + code);
            System.out.println("bytes captured: " + byteCounter[0]);
        }
    }

    private static class FfmpegDeviceLister {
        @SneakyThrows
        AvDevices listDevices() {
            Exec.Result exec;

            try {
                exec = Exec.INSTANCE.exec(Exec.Config.builder()
                        .command("ffmpeg").command("-f").command("avfoundation").command("-list_devices").command("true").command("-i").command("")
                        .build()
                );
            } catch (Exec.ExitCodeException e) {
                if (e.getResult().getCode() == 251)
                    exec = e.getResult();
                else
                    throw e;
            }

            System.out.println(new String(exec.getOut().readAllBytes()));
            var x = new String(exec.getErr().readAllBytes());
            System.out.println(x);
            System.out.println(exec);

            return new ParseAvFoundationDevices().apply(x.lines().toList());
        }
    }

    private static class ParseAvFoundationDevices implements Function<List<String>, AvDevices> {
        private static final Pattern SECTION_LINE = Pattern.compile("\\[AVFoundation(?: indev @ \\w{2,20})?] AVFoundation (?<section>\\w{1,10}) devices:");
        private static final Pattern DEVICE_LINE = Pattern.compile("\\[AVFoundation(?: indev @ \\w{2,20})?] \\[(?<index>\\d{1,3})] (?<name>[\\w ]{1,100})");

        @Override
        public AvDevices apply(List<String> strings) {
            var devices = new AvDevices()
                    .setAudioDevices(new ArrayList<>())
                    .setVideoDevices(new ArrayList<>());
            String section = null;
            for (var string : strings) {
                var sm = SECTION_LINE.matcher(string);
                if (sm.find()) {
                    section = sm.group("section");
                    continue;
                }

                var dm = DEVICE_LINE.matcher(string);
                if (dm.find()) {
                    var device = new AvDevices.AvDevice()
                            .setIndex(Integer.parseInt(dm.group("index")))
                            .setName(dm.group("name"));
                    switch (section) {
                        case "audio" -> devices.getAudioDevices().add(device);
                        case "video" -> devices.getVideoDevices().add(device);
                        case null, default ->
                                throw new UnsupportedOperationException("unknown section: " + section + " for device: " + device);
                    }
                }
            }

            return devices;
        }
    }

    @Data
    @Accessors(chain = true)
    private static class AvDevices {
        List<AvDevice> videoDevices;
        List<AvDevice> audioDevices;

        @Data
        @Accessors(chain = true)
        private static class AvDevice {
            int index;
            String name;
        }
    }
}
