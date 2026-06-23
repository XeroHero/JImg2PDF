package dev.xerohero;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainApp extends Application {

    private final List<BufferedImage> processedImagesQueue = new ArrayList<>();
    private final PdfCompiler pdfCompiler = new PdfCompiler();

    // UI elements we need to reference globally
    private ListView<String> fileListView;
    private Label statusLabel;
    private Button compileButton;
    private CheckBox aiEnhanceToggle;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("JImg2PDF - Professional Compiler");

        // --- ROOT LAYOUT ---
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f4f4f6;");

        // --- SIDEBAR CONTROL PANEL (Left Side) ---
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dcdcdc; -fx-border-width: 0 1 0 0;");

        Label controlsLabel = new Label("Control Options");
        controlsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Button browseButton = new Button("Add Images manually...");
        browseButton.setMaxWidth(Double.MAX_VALUE);

        aiEnhanceToggle = new CheckBox("Enable AI Deskewing");
        aiEnhanceToggle.setSelected(true);

        Separator separator = new Separator();

        compileButton = new Button("Compile PDF");
        compileButton.setMaxWidth(Double.MAX_VALUE);
        compileButton.setStyle("-fx-background-color: #0076ff; -fx-text-fill: white; -fx-font-weight: bold;");
        compileButton.setDisable(true);

        sidebar.getChildren().addAll(controlsLabel, browseButton, aiEnhanceToggle, separator, compileButton);
        root.setLeft(sidebar);

        // --- CENTER AREA (Drag Zone + File Queue Status) ---
        VBox centerArea = new VBox(15);
        centerArea.setPadding(new Insets(20));
        VBox.setVgrow(centerArea, Priority.ALWAYS);

        // Re-styled Drag and Drop Target Panel
        StackPane dropZone = new StackPane();
        dropZone.setPrefHeight(150);
        dropZone.setStyle("-fx-border-color: #a0a0a0; -fx-border-style: dashed; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-color: #eaeaea;");

        Label dropLabel = new Label("Drag & Drop Image Files Here");
        dropLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #444444; -fx-font-weight: bold;");
        dropZone.getChildren().add(dropLabel);

        // Queue File List Viewer
        fileListView = new ListView<>();
        VBox.setVgrow(fileListView, Priority.ALWAYS);

        Label queueLabel = new Label("Images in Compilation Queue:");
        queueLabel.setStyle("-fx-font-weight: bold;");

        centerArea.getChildren().addAll(dropZone, queueLabel, fileListView);
        root.setCenter(centerArea);

        // --- FOOTER STATUS BAR ---
        HBox statusBar = new HBox();
        statusBar.setPadding(new Insets(5, 15, 5, 15));
        statusBar.setStyle("-fx-background-color: #eaeaea; -fx-border-color: #cccccc; -fx-border-width: 1 0 0 0;");
        statusLabel = new Label("Ready");
        statusBar.getChildren().add(statusLabel);
        root.setBottom(statusBar);

        // --- EVENT WIREUP LOGIC ---

        // Manual File Selector Action
        browseButton.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Images");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png"));
            List<File> selectedFiles = chooser.showOpenMultipleDialog(primaryStage);
            if (selectedFiles != null) {
                for (File file : selectedFiles) {
                    processFile(file);
                }
            }
        });

        // Drag Hover Events
        dropZone.setOnDragOver(event -> {
            if (event.getGestureSource() != dropZone && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                dropZone.setStyle("-fx-border-color: #0076ff; -fx-border-style: dashed; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-color: #e0eeff;");
            }
            event.consume();
        });

        dropZone.setOnDragExited(event -> {
            dropZone.setStyle("-fx-border-color: #a0a0a0; -fx-border-style: dashed; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-color: #eaeaea;");
        });

        // Drop Execution Event
        dropZone.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                for (File file : db.getFiles()) {
                    String name = file.getName().toLowerCase();
                    if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")) {
                        processFile(file);
                    }
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        // PDF Generation Trigger
        compileButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Compiled PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File saveFile = fileChooser.showSaveDialog(primaryStage);

            if (saveFile != null) {
                try {
                    statusLabel.setText("Compiling PDF...");
                    pdfCompiler.compile(processedImagesQueue, saveFile);
                    statusLabel.setText("PDF compiled successfully to: " + saveFile.getName());
                    processedImagesQueue.clear();
                    fileListView.getItems().clear();
                    compileButton.setDisable(true);
                } catch (IOException ex) {
                    statusLabel.setText("Error writing PDF file.");
                    ex.printStackTrace();
                }
            }
        });

        primaryStage.setScene(new Scene(root, 850, 600));
        primaryStage.show();
    }

    private void processFile(File file) {
        fileListView.getItems().add(file.getName() + " (Processing...)");
        statusLabel.setText("AI is analyzing image perspectives...");

        String modelPath = System.getProperty("user.home") + "/.jimg2pdf/models/document_corners.onnx";
        ImageProcessingTask task = new ImageProcessingTask(file);

        task.setOnSucceeded(e -> {
            BufferedImage cleanImage = task.getValue();
            processedImagesQueue.add(cleanImage);

            // Update UI list representation state
            int index = fileListView.getItems().indexOf(file.getName() + " (Processing...)");
            if (index != -1) {
                fileListView.getItems().set(index, "[Enhanced] " + file.getName());
            }

            compileButton.setDisable(false);
            statusLabel.setText("Successfully imported " + file.getName());
        });

        task.setOnFailed(e -> {
            statusLabel.setText("AI failed for " + file.getName() + ". Using fallback.");
            int index = fileListView.getItems().indexOf(file.getName() + " (Processing...)");
            if (index != -1) {
                fileListView.getItems().set(index, "[Raw Match] " + file.getName());
            }
            // Fallback strategy: Read directly to buffer without deskewing
            try {
                processedImagesQueue.add(javax.imageio.ImageIO.read(file));
                compileButton.setDisable(false);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
}