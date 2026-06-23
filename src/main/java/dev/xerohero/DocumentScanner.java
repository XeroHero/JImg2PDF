package dev.xerohero;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.util.Collections;

public class DocumentScanner implements AutoCloseable {
    private final OrtEnvironment env;
    private final OrtSession session;
    private final int modelWidth = 512;  // Match your model's input dimension
    private final int modelHeight = 512;

    public DocumentScanner(String modelPath) throws OrtException {
        this.env = OrtEnvironment.getEnvironment();
        this.session = env.createSession(modelPath, new OrtSession.SessionOptions());
    }

    public float[] findCorners(BufferedImage originalImage) throws OrtException {
        // 1. Resize image to model dimensions
        BufferedImage resized = new BufferedImage(modelWidth, modelHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.drawImage(originalImage, 0, 0, modelWidth, modelHeight, null);
        g.dispose();

        // 2. Convert pixels to a flat float array (CHW format: RRR...GGG...BBB...)
        float[] floatPixels = new float[1 * 3 * modelWidth * modelHeight];
        int idx = 0;

        for (int c = 0; c < 3; c++) { // Channels
            for (int y = 0; y < modelHeight; y++) {
                for (int x = 0; x < modelWidth; x++) {
                    int rgb = resized.getRGB(x, y);
                    int val = switch (c) {
                        case 0 -> (rgb >> 16) & 0xFF; // Red
                        case 1 -> (rgb >> 8) & 0xFF;  // Green
                        default -> rgb & 0xFF;         // Blue
                    };
                    // Normalize pixel coordinates between 0.0 and 1.0 (or match model specs)
                    floatPixels[idx++] = val / 255.0f;
                }
            }
        }

        // 3. Package into an ONNX Tensor [1, 3, 512, 512]
        long[] shape = new long[]{1, 3, modelWidth, modelHeight};
        FloatBuffer buffer = FloatBuffer.wrap(floatPixels);

        try (OnnxTensor inputTensor = OnnxTensor.createTensor(env, buffer, shape)) {
            try (OrtSession.Result results = session.run(Collections.singletonMap("images", inputTensor))) {
                float[][] output = (float[][]) results.get(0).getValue();

                // 4. Map relative outputs back to original image dimensions
                float[] relativeCoordinates = output[0]; // e.g., [x0, y0, x1, y1...]
                return scaleCoordinatesBack(relativeCoordinates, originalImage.getWidth(), originalImage.getHeight());
            }
        }
    }

    private float[] scaleCoordinatesBack(float[] coords, int origW, int origH) {
        float[] scaled = new float[coords.length];
        for (int i = 0; i < coords.length; i += 2) {
            scaled[i] = coords[i] * origW;       // Scale X back
            scaled[i+1] = coords[i+1] * origH;   // Scale Y back
        }
        return scaled;
    }

    @Override
    public void close() throws OrtException {
        if (session != null) session.close();
        if (env != null) env.close();
    }
}