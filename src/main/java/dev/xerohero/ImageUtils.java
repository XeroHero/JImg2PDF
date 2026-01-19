package dev.xerohero;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.LookupOp;
import java.awt.image.ShortLookupTable;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageUtils {
    
    public static BufferedImage loadImage(File file) throws IOException {
        BufferedImage img = ImageIO.read(file);
        // Convert to ARGB to ensure consistent handling
        BufferedImage converted = new BufferedImage(
            img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = converted.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return converted;
    }
    
    public static BufferedImage rotateImage(BufferedImage image, double degrees) {
        double radians = Math.toRadians(degrees);
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));
        int newWidth = (int) Math.round(image.getWidth() * cos + image.getHeight() * sin);
        int newHeight = (int) Math.round(image.getWidth() * sin + image.getHeight() * cos);
        
        BufferedImage rotated = new BufferedImage(
            Math.max(1, newWidth), 
            Math.max(1, newHeight), 
            BufferedImage.TYPE_INT_ARGB);
            
        Graphics2D g2d = rotated.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                           RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
                           RenderingHints.VALUE_RENDER_QUALITY);
        
        AffineTransform at = new AffineTransform();
        at.translate((newWidth - image.getWidth()) / 2.0, 
                    (newHeight - image.getHeight()) / 2.0);
        at.rotate(radians, image.getWidth() / 2.0, image.getHeight() / 2.0);
        
        g2d.setTransform(at);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return rotated;
    }
    
    public static BufferedImage flipImage(BufferedImage image, boolean flipHorizontal, boolean flipVertical) {
        if (!flipHorizontal && !flipVertical) {
            return image;
        }
        
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage flipped = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = flipped.createGraphics();
        
        // Apply transformations
        if (flipHorizontal && flipVertical) {
            // Flip both horizontally and vertically
            g2d.drawImage(image, width, height, -width, -height, null);
        } else if (flipHorizontal) {
            // Flip horizontally
            g2d.drawImage(image, width, 0, -width, height, null);
        } else {
            // Flip vertically
            g2d.drawImage(image, 0, height, width, -height, null);
        }
        
        g2d.dispose();
        return flipped;
    }
    
    public static BufferedImage adjustBrightnessContrast(BufferedImage image, int brightness, int contrast) {
        // Convert brightness from -100..100 to -255..255
        float brightnessFactor = brightness * 2.55f;
        
        // Convert contrast from -100..100 to 0..4
        float contrastFactor = (contrast + 100f) / 100f;
        contrastFactor *= contrastFactor; // Quadratic scaling for better visual response
        
        // Create lookup table for brightness and contrast
        short[] brightTable = new short[256];
        for (int i = 0; i < 256; i++) {
            // Apply contrast first (centered around 128)
            int value = (int) ((i - 128) * contrastFactor + 128);
            // Then apply brightness
            value += brightnessFactor;
            // Clamp to 0-255
            brightTable[i] = (short) Math.max(0, Math.min(255, value));
        }
        
        // Create lookup table for alpha (unchanged)
        short[] alphaTable = new short[256];
        for (int i = 0; i < 256; i++) {
            alphaTable[i] = (short) i;
        }
        
        // Create lookup tables for each color component
        short[][] lookupTables = {
            brightTable, // Red
            brightTable, // Green
            brightTable, // Blue
            alphaTable   // Alpha (unchanged)
        };
        
        // Apply the lookup operation
        BufferedImageOp op = new LookupOp(
            new ShortLookupTable(0, lookupTables), null);
            
        return op.filter(image, null);
    }
    
    public static void saveImage(BufferedImage image, File file) throws IOException {
        String formatName = getFileExtension(file).toLowerCase();
        
        // Create a new image with the correct color model for the output format
        BufferedImage outputImage;
        if (formatName.equals("jpg") || formatName.equals("jpeg")) {
            // For JPEG, convert to RGB (no alpha)
            outputImage = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = outputImage.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
            g.drawImage(image, 0, 0, null);
            g.dispose();
        } else if (formatName.equals("png")) {
            // For PNG, keep transparency
            outputImage = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = outputImage.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
        } else {
            // For other formats, use the original image
            outputImage = image;
        }
        
        // Write the image
        ImageIO.write(outputImage, formatName, file);
    }
    
    private static String getFileExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return lastDot == -1 ? "" : name.substring(lastDot + 1);
    }
    
    /**
     * Applies multiple image adjustments in the correct order.
     * @param image The source image to adjust
     * @param rotation Rotation in degrees
     * @param flipHorizontal Whether to flip the image horizontally
     * @param flipVertical Whether to flip the image vertically
     * @param brightness Brightness adjustment (-100 to 100)
     * @param contrast Contrast adjustment (-100 to 100)
     * @return The adjusted image
     */
    public static BufferedImage applyAdjustmentsToImage(
            BufferedImage image, 
            int rotation, 
            boolean flipHorizontal, 
            boolean flipVertical, 
            int brightness, 
            int contrast) {
        
        if (image == null) {
            return null;
        }
        
        // Work on a copy to avoid modifying the original
        BufferedImage result = new BufferedImage(
            image.getWidth(), 
            image.getHeight(), 
            BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        
        // Apply adjustments in the correct order:
        // 1. Brightness/Contrast (pixel operations first)
        result = adjustBrightnessContrast(result, brightness, contrast);
        
        // 2. Flip (geometric operation before rotation)
        result = flipImage(result, flipHorizontal, flipVertical);
        
        // 3. Rotation (last geometric operation)
        if (rotation != 0) {
            result = rotateImage(result, rotation);
        }
        
        return result;
    }
}
