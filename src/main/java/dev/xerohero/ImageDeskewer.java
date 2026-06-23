package dev.xerohero;

import boofcv.alg.distort.RemovePerspectiveDistortion;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.Planar;
import georegression.struct.point.Point2D_F64;
import java.awt.image.BufferedImage;

public class ImageDeskewer {

    public BufferedImage straighten(BufferedImage inputImage,
                                    double x0, double y0,  // Top-Left
                                    double x1, double y1,  // Top-Right
                                    double x2, double y2,  // Bottom-Right
                                    double x3, double y3)  // Bottom-Left
    {
        // 1. Convert the multi-channel BufferedImage to a BoofCV Planar RGB image
        Planar<GrayU8> input = ConvertBufferedImage.convertFromPlanar(inputImage, null, true, GrayU8.class);

        // 2. Calculate the approximate bounds for the output image dimension scaling
        int outputWidth = (int) Math.max(Math.abs(x1 - x0), Math.abs(x2 - x3));
        int outputHeight = (int) Math.max(Math.abs(y3 - y0), Math.abs(y2 - y1));

        if (outputWidth <= 0 || outputHeight <= 0) {
            return inputImage;
        }

        // 3. Instantiate with width, height, and explicit ImageType directly in the constructor
        RemovePerspectiveDistortion<Planar<GrayU8>> remover = new RemovePerspectiveDistortion<>(
                outputWidth,
                outputHeight,
                ImageType.pl(3, GrayU8.class) // Declares a 3-channel Planar GrayU8 image
        );

        // 4. Wrap the raw coordinates into BoofCV's Point2D_F64 structs
        Point2D_F64 p0 = new Point2D_F64(x0, y0);
        Point2D_F64 p1 = new Point2D_F64(x1, y1);
        Point2D_F64 p2 = new Point2D_F64(x2, y2);
        Point2D_F64 p3 = new Point2D_F64(x3, y3);

        // 5. Execute the distortion removal step
        if (remover.apply(input, p0, p1, p2, p3)) {
            // Retrieve the internally generated output image
            Planar<GrayU8> output = remover.getOutput();
            // Convert back to a standard BufferedImage
            return ConvertBufferedImage.convertTo(output, null, true);
        }

        return inputImage; // Fallback
    }
}