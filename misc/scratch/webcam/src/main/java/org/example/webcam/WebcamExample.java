// package org.example.webcam;
//
// import com.github.sarxos.webcam.Webcam;
// import com.github.sarxos.webcam.WebcamResolution;
// import com.github.sarxos.webcam.;
// import lombok.SneakyThrows;
//
// import javax.imageio.ImageIO;
// import java.awt.image.BufferedImage;
// import java.io.File;
// import java.io.IOException;
// import java.util.List;
//
// public class WebcamExample {
//     @SneakyThrows
//     static void main() {
//         // Get all capture devices
//
//         Webcam.setDriver(new JmfDriver());
//         List<Webcam> webcams = Webcam.getWebcams();
//
//         System.out.println(webcams.size());
//         System.out.println(webcams);
//
//         // extracted();
//     }
//
//     private static void extracted() throws IOException {
//         // Select a device
//         Webcam webcam = Webcam.getDefault();
//         if (webcam != null) {
//             webcam.setViewSize(WebcamResolution.VGA.getSize());
//             webcam.open();
//             // Take picture
//             BufferedImage image = webcam.getImage();
//             ImageIO.write(image, "JPG", new File("test.jpg"));
//             webcam.close();
//         }
//     }
// }
