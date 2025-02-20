//WebcamCapture.java
package client;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

public class WebcamCapture {

    private static Webcam webcam = null;

    public static BufferedImage captureWebcam() {
        try {
            if (webcam == null) {
                // Set preferred resolution (adjust as needed)
                Dimension[] nonStandardResolutions = new Dimension[]{
                        WebcamResolution.VGA.getSize(),
                };
                webcam = Webcam.getDefault();
                webcam.setCustomViewSizes(nonStandardResolutions);
                webcam.setViewSize(WebcamResolution.VGA.getSize());

                if (!webcam.isOpen()) {
                    webcam.open();
                }
            }

            return webcam.getImage();
        } catch (Exception e) {
            System.err.println("Error capturing webcam: " + e.getMessage());
            return null;
        }
    }
}