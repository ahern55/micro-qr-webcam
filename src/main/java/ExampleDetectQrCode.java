import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

import boofcv.abst.fiducial.MicroQrCodeDetector;
import boofcv.abst.fiducial.QrCodeDetector;
import boofcv.alg.fiducial.microqr.MicroQrCode;
import boofcv.alg.fiducial.qrcode.QrCode;
import boofcv.factory.fiducial.ConfigMicroQrCode;
import boofcv.factory.fiducial.ConfigQrCode;
import boofcv.factory.fiducial.FactoryFiducial;
import boofcv.gui.image.ImagePanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.UtilIO;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayU8;
import boofcv.gui.feature.VisualizeShapes;
import com.github.sarxos.webcam.Webcam;
import georegression.struct.point.Point2D_F64;

/**
 * Shows you how to detect a QR Code inside an image and process the extracted data. Much of the information that
 * is computed while detecting and decoding a QR Code is saved inside the {@link QrCode} class. This can be useful
 * for application developers.
 *
 * @author Peter Abeles
 */
public class ExampleDetectQrCode {
    public static void main( String[] args ) {

        Webcam webcam = Webcam.getDefault();
        webcam.open();
        var config = new ConfigMicroQrCode();
        MicroQrCodeDetector<GrayU8> detector = FactoryFiducial.microqr(config, GrayU8.class);

        BufferedImage input = webcam.getImage();
        ImagePanel imagePanel = ShowImages.showWindow(input, "Example Micro QR Codes", true);

        while (true) {
            input = webcam.getImage();
            GrayU8 gray = ConvertBufferedImage.convertFrom(input, (GrayU8) null);


//		config.considerTransposed = false; // by default, it will consider incorrectly encoded markers. Faster if false


            detector.process(gray);

            // Gets a list of all the qr codes it could successfully detect and decode
            List<MicroQrCode> detections = detector.getDetections();

            Graphics2D g2 = input.createGraphics();
            int strokeWidth = Math.max(4, input.getWidth() / 200); // in large images the line can be too thin
            g2.setColor(Color.GREEN);
            g2.setStroke(new BasicStroke(strokeWidth));
            for (MicroQrCode qr : detections) {
                System.out.println(qr.bounds.toString());
                // The message encoded in the marker
                System.out.println("message: '" + qr.message + "'");

                // Visualize its location in the image
                VisualizeShapes.drawPolygon(qr.bounds, true, 1, g2);
                VisualizeShapes.draw(qr.bounds.vertexes.get(0), qr.bounds.vertexes.get(2), g2);
            }

            // List of objects it thinks might be a QR Code but failed for various reasons
            List<MicroQrCode> failures = detector.getFailures();
            g2.setColor(Color.RED);
            for (MicroQrCode qr : failures) {
                // If the 'cause' is ERROR_CORRECTION or later it might a real QR Code
                if (qr.failureCause.ordinal() < QrCode.Failure.ERROR_CORRECTION.ordinal())
                    continue;

                VisualizeShapes.drawPolygon(qr.bounds, true, 1, g2);
            }


            imagePanel.setImageUI(input);
        }
    }
}