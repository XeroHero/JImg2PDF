package dev.xerohero;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple utility to convert multiple images into a single PDF file.
 * Usage: java -jar JImg2PDF.jar <output.pdf> <image1> [<image2> ...]
 */
public class Main {
    
    public static void main(String[] args) {
        if (args.length < 2) {
            printUsage();
            System.exit(1);
        }

        String outputPath = args[0];
        List<File> imageFiles = new ArrayList<>();

        // Collect all image files
        for (int i = 1; i < args.length; i++) {
            Path path = Paths.get(args[i]);
            if (Files.exists(path) && !Files.isDirectory(path)) {
                imageFiles.add(path.toFile());
            } else {
                System.err.println("Warning: File not found or is a directory: " + path);
            }
        }

        if (imageFiles.isEmpty()) {
            System.err.println("Error: No valid image files provided");
            printUsage();
            System.exit(1);
        }

        // Convert images to PDF
        try {
            System.out.println("Converting " + imageFiles.size() + " images to PDF...");
            ImageToPdfConverter.convertImagesToPdf(imageFiles, outputPath);
            System.out.println("Conversion completed successfully!");
        } catch (Exception e) {
            System.err.println("Error converting images to PDF: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.out.println("Image to PDF Converter");
        System.out.println("Usage: java -jar JImg2PDF.jar <output.pdf> <image1> [<image2> ...]");
        System.out.println("\nExample:");
        System.out.println("  java -jar JImg2PDF.jar output.pdf image1.jpg image2.png image3.bmp");
        System.out.println("\nSupported image formats: JPG, JPEG, PNG, BMP, GIF, TIF, TIFF");
    }
}
