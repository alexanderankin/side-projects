package org.example.webcam;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import lombok.SneakyThrows;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
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
            var qrStream = imageStream.filter(Objects::nonNull)
                    // .peek(this::preview)
                    .map(this::decodeQR);

            var counter = new AtomicInteger();
            qrStream.forEach(string -> {
                System.out.println(counter.incrementAndGet() + ": " + string);
            });
        }
    }

    @SneakyThrows
    private void preview(BufferedImage bufferedImage) {
        JFrame frame = new JFrame();
        frame.setSize(400, 400);
        frame.getContentPane().setLayout(new FlowLayout());
        frame.getContentPane().add(new JLabel(new ImageIcon(bufferedImage)));
        var button = new JButton();
        frame.getContentPane().add(button);
        frame.pack();

        var cdl = new CountDownLatch(1);
        var closed = new AtomicBoolean(false);
        var timer = new Timer[1];
        Runnable close = () -> {
            if (closed.compareAndSet(false, true)) {
                frame.setVisible(false);
                frame.dispose();
                cdl.countDown();
                if (timer[0] != null)
                    timer[0].stop();
            }
        };

        button.setText("close");
        button.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close.run();
            }
        });
        frame.getRootPane().getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "CLOSE");
        frame.getRootPane().getActionMap().put("CLOSE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close.run();
            }
        });

        timer[0] = new Timer(10_000, ignored -> close.run());
        timer[0].start();

        frame.setVisible(true);
        cdl.await();
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
