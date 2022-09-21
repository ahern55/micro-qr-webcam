import boofcv.abst.fiducial.MicroQrCodeDetector;
import boofcv.alg.fiducial.microqr.MicroQrCode;
import boofcv.alg.fiducial.qrcode.QrCode;
import boofcv.factory.fiducial.ConfigMicroQrCode;
import boofcv.factory.fiducial.FactoryFiducial;
import boofcv.gui.feature.VisualizeShapes;
import boofcv.gui.image.ImagePanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayU8;
import config.Configuration;
import config.RegionCalibration;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

public class WebcamDetectQRCode {
    public static void main(String[] args) throws FrameGrabber.Exception {
        Configuration configs = Configuration.getConfiguration();
        RegionCalibration region = configs.calibration.get(0);

        FrameGrabber grabber = new OpenCVFrameGrabber(0);
        Java2DFrameConverter converter = new Java2DFrameConverter();
        grabber.start();

        var config = new ConfigMicroQrCode();
        MicroQrCodeDetector<GrayU8> detector = FactoryFiducial.microqr(config, GrayU8.class);

        BufferedImage input = converter.convert(grabber.grab());
        ImagePanel imagePanel = ShowImages.showWindow(input, "Example Micro QR Codes", true);

        while (true) {
            input = converter.convert(grabber.grab());
            GrayU8 gray = ConvertBufferedImage.convertFrom(input, (GrayU8) null);

            detector.process(gray);

            // Gets a list of all the qr codes it could successfully detect and decode
            List<MicroQrCode> detections = detector.getDetections();

            Graphics2D g2 = input.createGraphics();
            int strokeWidth = Math.max(4, input.getWidth() / 200); // in large images the line can be too thin
            g2.setColor(Color.gray);
            g2.setStroke(new BasicStroke((float) strokeWidth / 2));

            VisualizeShapes.drawRectangle(region.x1, region.y1, region.x2, region.y2, new Line2D.Double(), g2);
            g2.setColor(Color.GREEN);
            g2.setStroke(new BasicStroke(strokeWidth));

            for (MicroQrCode qr : detections) {
                // Visualize its location in the image
                VisualizeShapes.drawPolygon(qr.bounds, true, 1, g2);
                VisualizeShapes.draw(qr.bounds.vertexes.get(0), qr.bounds.vertexes.get(2), g2);

                Position p = Position.fromPolygon(qr.bounds);
                g2.drawString("Heading: " + p.heading, 100, 115);

                g2.drawString("message: '" + qr.message + "'", 100, 100);
            }

//            // List of objects it thinks might be a QR Code but failed for various reasons
//            List<MicroQrCode> failures = detector.getFailures();
//            g2.setColor(Color.RED);
//            for (MicroQrCode qr : failures) {
//                // If the 'cause' is ERROR_CORRECTION or later it might a real QR Code
//                if (qr.failureCause.ordinal() < QrCode.Failure.ERROR_CORRECTION.ordinal())
//                    continue;
//
//                VisualizeShapes.drawPolygon(qr.bounds, true, 1, g2);
//            }

            imagePanel.setImageUI(input);
        }
    }
}