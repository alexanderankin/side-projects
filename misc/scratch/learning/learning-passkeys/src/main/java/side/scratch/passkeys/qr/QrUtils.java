package side.scratch.passkeys.qr;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class QrUtils {
    public static String parseQrText(String filename) throws Exception {
        BufferedImage image = ImageIO.read(new File(filename));
        if (image == null) {
            throw new IllegalArgumentException("Could not read image: " + filename);
        }
        return parseQrText(image);
    }

    public static String parseQrText(BufferedImage image) throws Exception {
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
        Result result = new MultiFormatReader().decode(bitmap);
        return result.getText();
    }
}
