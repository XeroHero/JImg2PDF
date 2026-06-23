package dev.xerohero;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class PdfCompiler {

    /**
     * Compiles a list of enhanced in-memory BufferedImages into a single PDF file.
     */
    public void compile(List<BufferedImage> images, File outputFile) throws IOException {
        try (PDDocument document = new PDDocument()) {

            for (BufferedImage img : images) {
                addImageToPdf(document, img);
            }

            // Save the compiled document to the destination path chosen by the user
            document.save(outputFile);
        }
    }

    /**
     * Your PDFBox block for mapping a single buffered image onto a new document page.
     */
    private void addImageToPdf(PDDocument document, BufferedImage bufferedImage) throws IOException {
        PDPage page = new PDPage();
        document.addPage(page);

        // Convert BufferedImage into a PDFBox compatible graphic object directly without disk I/O
        PDImageXObject imageXObject = LosslessFactory.createFromImage(document, bufferedImage);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            // Draw image matching the upper bounds of the page size scaling
            contentStream.drawImage(imageXObject, 0, 0, page.getMediaBox().getWidth(), page.getMediaBox().getHeight());
        }
    }
}