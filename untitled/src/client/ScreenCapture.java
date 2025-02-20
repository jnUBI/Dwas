package client;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ScreenCapture {
    public static byte[] captureScreen() {
        try {
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage capture = new Robot().createScreenCapture(screenRect);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(capture, "jpeg", baos); // Or "png" if you need lossless. Jpeg is faster and smaller
            return baos.toByteArray();
        } catch (AWTException | IOException e) {
            System.err.println("Error capturing screen: " + e.getMessage());
            return null;
        }
    }
}