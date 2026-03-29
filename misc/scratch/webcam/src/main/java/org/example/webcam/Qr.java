package org.example.webcam;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import lombok.SneakyThrows;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class Qr implements Runnable {

    static void main() {
        new Qr().run();
    }

    @SneakyThrows
    public void run() {
        try (var inputStream = Files.newInputStream(Path.of("capture.mkv"));
             var imageStream = images(inputStream)) {
            var qrStream = imageStream.filter(Objects::nonNull).map(this::decodeQR);

            var counter = new AtomicInteger();
            qrStream.forEach(string -> {
                System.out.println(counter.incrementAndGet() + ": " + string);
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
                Frame frame = grabber.grabImage();
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
        return imageStream.takeWhile(Objects::nonNull).onClose(close);
    }

    String decodeQR(BufferedImage image) {
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();
        } catch (NotFoundException e) {
            return null; // no QR in this frame
        }
    }
}
