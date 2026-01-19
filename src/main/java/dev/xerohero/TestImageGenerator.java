package dev.xerohero;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TestImageGenerator {
    public static void main(String[] args) {
        createTestImage("test1.png", "Test Image 1", Color.RED);
        createTestImage("test2.png", "Test Image 2", Color.GREEN);
        createTestImage("test3.png", "Test Image 3", Color.BLUE);
        System.out.println("Created test images: test1.png, test2.png, test3.png");
    }

    private static void createTestImage(String filename, String text, Color color) {
        int width = 400;
        int height = 300;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        // Fill background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        
        // Draw colored rectangle
        g.setColor(color);
        g.fillRect(50, 50, width - 100, height - 100);
        
        // Draw text
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        FontMetrics metrics = g.getFontMetrics();
        int x = (width - metrics.stringWidth(text)) / 2;
        int y = ((height - metrics.getHeight()) / 2) + metrics.getAscent();
        g.drawString(text, x, y);
        
        // Save the image
        try {
            ImageIO.write(image, "PNG", new File(filename));
        } catch (IOException e) {
            System.err.println("Error creating test image: " + e.getMessage());
        }
    }
}
