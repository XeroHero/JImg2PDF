package dev.xerohero;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ImageToPdfConverter {

    /**
     * Converts a list of image files to a single PDF file.
     *
     * @param imageFiles List of image files to convert
     * @param outputPdfPath Path where the output PDF will be saved
     * @throws IOException If an I/O error occurs
     */
    public static void convertImagesToPdf(List<File> imageFiles, String outputPdfPath) throws IOException {
        if (imageFiles == null || imageFiles.isEmpty()) {
            throw new IllegalArgumentException("No image files provided");
        }

        try (PDDocument document = new PDDocument()) {
            for (File imageFile : imageFiles) {
                if (!imageFile.exists() || !imageFile.canRead()) {
                    System.err.println("Skipping unreadable file: " + imageFile.getAbsolutePath());
                    continue;
                }

                try {
                    // Read the image file into a byte array first
                    byte[] imageData = java.nio.file.Files.readAllBytes(imageFile.toPath());
                    String formatName = detectImageFormat(imageData);
                    
                    if (formatName == null) {
                        System.err.println("Unsupported image format for file: " + imageFile.getAbsolutePath());
                        continue;
                    }
                    
                    // Create PDImageXObject using the detected format
                    PDImageXObject pdImage;
                    try (InputStream in = new ByteArrayInputStream(imageData)) {
                        // First try to create directly from the stream
                        try {
                            pdImage = PDImageXObject.createFromByteArray(document, imageData, imageFile.getName());
                        } catch (Exception e) {
                            // If direct creation fails, try loading through BufferedImage
                            BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageData));
                            if (img == null) {
                                throw new IOException("Failed to read image data");
                            }
                            
                            // Convert BufferedImage to byte array with correct format
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            if (!ImageIO.write(img, formatName, baos)) {
                                throw new IOException("No appropriate writer found for format: " + formatName);
                            }
                            
                            // Create from the properly formatted byte array
                            pdImage = PDImageXObject.createFromByteArray(
                                document, 
                                baos.toByteArray(), 
                                imageFile.getName()
                            );
                        }
                    }
                    
                    // Create page with the same dimensions as the image
                    PDRectangle pageSize = new PDRectangle(pdImage.getWidth(), pdImage.getHeight());
                    PDPage page = new PDPage(pageSize);
                    document.addPage(page);

                    // Draw the image on the page
                    try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                        contentStream.drawImage(pdImage, 0, 0, pdImage.getWidth(), pdImage.getHeight());
                    }
                } catch (IOException e) {
                    System.err.println("Error processing file: " + imageFile.getAbsolutePath());
                    e.printStackTrace();
                }
            }

            if (document.getNumberOfPages() == 0) {
                throw new IOException("No valid images were processed");
            }

            // Save the final PDF
            document.save(outputPdfPath);
            System.out.println("PDF created successfully at: " + new File(outputPdfPath).getAbsolutePath());
        }
    }
    
    /**
     * Detects the image format based on the file's magic numbers.
     * @param data The image file data as a byte array
     * @return The image format name (e.g., "jpg", "png") or null if format is not recognized
     */
    private static String detectImageFormat(byte[] data) {
        if (data.length < 4) {
            return null;
        }
        
        // Check for JPEG
        if ((data[0] == (byte) 0xFF && data[1] == (byte) 0xD8 && data[2] == (byte) 0xFF)) {
            return "jpg";
        }
        
        // Check for PNG
        if (data[0] == (byte) 0x89 && data[1] == (byte) 0x50 && 
            data[2] == (byte) 0x4E && data[3] == (byte) 0x47) {
            return "png";
        }
        
        // Check for GIF
        if (data[0] == 'G' && data[1] == 'I' && data[2] == 'F' && data[3] == '8') {
            return "gif";
        }
        
        // Check for BMP
        if (data[0] == 'B' && data[1] == 'M') {
            return "bmp";
        }
        
        // Check for TIFF (little endian)
        if (data[0] == 0x49 && data[1] == 0x49 && data[2] == 0x2A && data[3] == 0x00) {
            return "tif";
        }
        
        // Check for TIFF (big endian)
        if (data[0] == 0x4D && data[1] == 0x4D && data[2] == 0x00 && data[3] == 0x2A) {
            return "tif";
        }
        
        return null;
    }
}
