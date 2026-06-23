package dev.xerohero;

import javafx.concurrent.Task;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.imageio.ImageIO;

public class ImageProcessingTask extends Task<BufferedImage> {
    private final File imageFile;

    public ImageProcessingTask(File imageFile) {
        this.imageFile = imageFile;
    }

    @Override
    protected BufferedImage call() throws Exception {
        // 1. Read input image straight into an in-memory buffer
        BufferedImage rawImg = ImageIO.read(imageFile);
        if (rawImg == null) {
            throw new java.io.IOException("Failed to read image data: " + imageFile.getName());
        }

        // 2. Safely look for your internal resource model string
        InputStream is = getClass().getResourceAsStream("/model/document_corners.onnx");

        if (is != null) {
            // AI ROUTE: Run your ONNX Session if you bundle a model later on
            try {
                File tempModelFile = File.createTempFile("document_corners", ".onnx");
                tempModelFile.deleteOnExit();
                Files.copy(is, tempModelFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                is.close();

                try (DocumentScanner scanner = new DocumentScanner(tempModelFile.getAbsolutePath())) {
                    ImageDeskewer deskewer = new ImageDeskewer();
                    float[] corners = scanner.findCorners(rawImg);
                    return deskewer.straighten(
                            rawImg,
                            corners[0], corners[1],
                            corners[2], corners[3],
                            corners[4], corners[5],
                            corners[6], corners[7]
                    );
                }
            } catch (Exception ex) {
                System.err.println("AI session failed, routing to native CV frame mapping.");
            }
        }

        // 3. GEOMETRIC FALLBACK ROUTE: Pass the image straight through to the deskewer bounds
        // This ensures your queue works right now!
        ImageDeskewer deskewer = new ImageDeskewer();
        return deskewer.straighten(
                rawImg,
                0, 0,                                  // Top-Left
                rawImg.getWidth(), 0,                  // Top-Right
                rawImg.getWidth(), rawImg.getHeight(), // Bottom-Right
                0, rawImg.getHeight()                  // Bottom-Left
        );
    }
}