package dev.xerohero;

import dev.xerohero.model.ImageAdjustment;
import dev.xerohero.theme.ThemeManager;
import dev.xerohero.ui.UIComponentFactory;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ImageAdjustmentDialog extends JDialog implements ActionListener, ChangeListener {
    // UI Components
    private JSlider rotationSlider;
    private JCheckBox flipHorizontalCheckbox;
    private JCheckBox flipVerticalCheckbox;
    private JSlider brightnessSlider;
    private JSlider contrastSlider;
    private JLabel previewLabel;
    private JLabel imageCounter;

    // Data
    private final List<File> imageFiles;
    private final List<ImageAdjustment> adjustments;
    private BufferedImage currentImage;
    private File currentFile;
    private int currentIndex = 0;

    // Services
    private final UIComponentFactory uiFactory;
    private final ThemeManager themeManager;

    public ImageAdjustmentDialog(Frame owner, List<File> imageFiles) {
        super(owner, "Adjust Images", true);
        this.uiFactory = UIComponentFactory.getInstance();
        this.themeManager = ThemeManager.getInstance();
        this.imageFiles = new ArrayList<>(imageFiles);
        this.adjustments = new ArrayList<>();

        // Initialize adjustments for each image
        for (int i = 0; i < imageFiles.size(); i++) {
            adjustments.add(new ImageAdjustment());
        }

        // Setup dialog
        setupDialog();
        initComponents();

        // Load first image
        loadImage(0);
        updatePreview();
        updateImageCounter();

        // Apply initial theme
        themeManager.applyTheme(this);
    }

    private void setupDialog() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getRootPane().putClientProperty("apple.awt.brushMetalLook", Boolean.TRUE);
        setSize(800, 600);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));
    }

    private void initComponents() {
        // Create main panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();

                // Draw a subtle gradient background
                Color bgColor = UIManager.getColor("Panel.background");
                Color startColor = bgColor.brighter();
                Color endColor = bgColor.darker();

                GradientPaint gradient = new GradientPaint(
                        0, 0, startColor,
                        getWidth(), getHeight(), endColor
                );

                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };

        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Add theme toggle button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topPanel.setOpaque(false);
        JButton themeToggleButton = uiFactory.createThemeToggleButton();
        topPanel.add(themeToggleButton);

        // Create and add components
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(createPreviewPanel(), BorderLayout.CENTER);
        mainPanel.add(uiFactory.createVerticalDivider(), BorderLayout.EAST);
        mainPanel.add(createControlsPanel(), BorderLayout.EAST);

        add(mainPanel, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createPreviewPanel() {
        JPanel previewPanel = new JPanel(new BorderLayout(10, 10));
        previewPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Preview label for the image
        previewLabel = new JLabel("", JLabel.CENTER);
        previewLabel.setOpaque(true);
        previewLabel.setBackground(Color.WHITE);
        previewLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));

        // Navigation panel
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        navPanel.setOpaque(false);

        JButton prevButton = uiFactory.createStyledButton("< Previous");
        JButton nextButton = uiFactory.createStyledButton("Next >");
        imageCounter = new JLabel("1/" + imageFiles.size());
        imageCounter.setFont(imageCounter.getFont().deriveFont(Font.BOLD));

        // Add action listeners
        prevButton.addActionListener(e -> showPreviousImage());
        nextButton.addActionListener(e -> showNextImage());

        // Add components to navigation panel
        navPanel.add(prevButton);
        navPanel.add(imageCounter);
        navPanel.add(nextButton);

        // Add components to preview panel
        previewPanel.add(previewLabel, BorderLayout.CENTER);
        previewPanel.add(navPanel, BorderLayout.SOUTH);

        return previewPanel;
    }

    private JPanel createControlsPanel() {
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
        controlsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add control groups
        controlsPanel.add(createRotationControls());
        controlsPanel.add(Box.createVerticalStrut(15));
        controlsPanel.add(createFlipControls());
        controlsPanel.add(Box.createVerticalStrut(15));
        controlsPanel.add(createBrightnessControls());
        controlsPanel.add(Box.createVerticalStrut(15));
        controlsPanel.add(createContrastControls());
        controlsPanel.add(Box.createVerticalStrut(20));

        // Add reset button
        JButton resetButton = uiFactory.createStyledButton("Reset Adjustments");
        resetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        resetButton.addActionListener(e -> resetAdjustments());
        controlsPanel.add(resetButton);

        return controlsPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setOpaque(false);

        JButton applyAllButton = uiFactory.createStyledButton("Apply to All");
        JButton okButton = uiFactory.createStyledButton("OK");
        JButton cancelButton = uiFactory.createStyledButton("Cancel");

        // Set default button
        getRootPane().setDefaultButton(okButton);

        // Add action listeners
        applyAllButton.addActionListener(e -> applyToAll());
        okButton.addActionListener(e -> applyAndClose());
        cancelButton.addActionListener(e -> dispose());

        // Add buttons to panel
        buttonPanel.add(applyAllButton);
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        return buttonPanel;
    }

    private JPanel createRotationControls() {
        JPanel panel = uiFactory.createControlGroup("Rotation");
        rotationSlider = uiFactory.createStyledSlider(-180, 180, 0, 90);
        rotationSlider.addChangeListener(e -> updatePreview());
        panel.add(rotationSlider);
        return panel;
    }

    private JPanel createFlipControls() {
        JPanel panel = uiFactory.createControlGroup("Flip");
        JPanel flipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        flipPanel.setOpaque(false);

        flipHorizontalCheckbox = new JCheckBox("Horizontal");
        flipVerticalCheckbox = new JCheckBox("Vertical");

        uiFactory.styleCheckbox(flipHorizontalCheckbox);
        uiFactory.styleCheckbox(flipVerticalCheckbox);

        flipHorizontalCheckbox.addActionListener(e -> updatePreview());
        flipVerticalCheckbox.addActionListener(e -> updatePreview());

        flipPanel.add(flipHorizontalCheckbox);
        flipPanel.add(flipVerticalCheckbox);
        panel.add(flipPanel);

        return panel;
    }

    private JPanel createBrightnessControls() {
        JPanel panel = uiFactory.createControlGroup("Brightness");
        brightnessSlider = uiFactory.createStyledSlider(-100, 100, 0, 50);
        brightnessSlider.addChangeListener(e -> updatePreview());
        panel.add(brightnessSlider);
        return panel;
    }

    private JPanel createContrastControls() {
        JPanel panel = uiFactory.createControlGroup("Contrast");
        contrastSlider = uiFactory.createStyledSlider(-100, 100, 0, 50);
        contrastSlider.addChangeListener(e -> updatePreview());
        panel.add(contrastSlider);
        return panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        switch (command) {
            case "< Previous":
                showPreviousImage();
                break;
            case "Next >":
                showNextImage();
                break;
            case "Reset Adjustments":
                resetAdjustments();
                break;
            case "Apply to All":
                applyToAll();
                break;
            case "OK":
                applyAndClose();
                break;
            case "Cancel":
                dispose();
                break;
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        updatePreview();
    }

    private void loadImage(int index) {
        if (index < 0 || index >= imageFiles.size()) return;

        currentIndex = index;
        currentFile = imageFiles.get(index);

        try {
            currentImage = javax.imageio.ImageIO.read(Objects.requireNonNull(currentFile));
            updateImageCounter();
            updatePreview();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading image: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveCurrentAdjustment() {
        if (currentIndex >= 0 && currentIndex < adjustments.size()) {
            ImageAdjustment adjustment = adjustments.get(currentIndex);
            adjustment.setRotation(rotationSlider.getValue());
            adjustment.setFlipHorizontal(flipHorizontalCheckbox.isSelected());
            adjustment.setFlipVertical(flipVerticalCheckbox.isSelected());
            adjustment.setBrightness(brightnessSlider.getValue());
            adjustment.setContrast(contrastSlider.getValue());
        }
    }

    private void updatePreview() {
        if (currentImage == null) return;

        // Get current adjustments
        int rotation = rotationSlider.getValue();
        boolean flipH = flipHorizontalCheckbox.isSelected();
        boolean flipV = flipVerticalCheckbox.isSelected();
        int brightness = brightnessSlider.getValue();
        int contrast = contrastSlider.getValue();

        // Apply adjustments to a copy of the image
        BufferedImage adjustedImage = ImageUtils.applyAdjustmentsToImage(
                currentImage, rotation, flipH, flipV, brightness, contrast);

        // Scale image to fit in the preview
        ImageIcon icon = new ImageIcon(adjustedImage.getScaledInstance(
                previewLabel.getWidth() > 0 ? previewLabel.getWidth() : 400,
                previewLabel.getHeight() > 0 ? previewLabel.getHeight() : 400,
                Image.SCALE_SMOOTH
        ));

        previewLabel.setIcon(icon);
    }

    private void updateImageCounter() {
        if (imageCounter != null) {
            imageCounter.setText((currentIndex + 1) + "/" + imageFiles.size());
        }
    }

    private void showPreviousImage() {
        if (currentIndex > 0) {
            saveCurrentAdjustment();
            loadImage(currentIndex - 1);
        }
    }

    private void showNextImage() {
        if (currentIndex < imageFiles.size() - 1) {
            saveCurrentAdjustment();
            loadImage(currentIndex + 1);
        }
    }

    private void resetAdjustments() {
        rotationSlider.setValue(0);
        flipHorizontalCheckbox.setSelected(false);
        flipVerticalCheckbox.setSelected(false);
        brightnessSlider.setValue(0);
        contrastSlider.setValue(0);
        updatePreview();
    }

    private void applyToAll() {
        int response = JOptionPane.showConfirmDialog(this,
                "Apply current adjustments to all images? This cannot be undone.",
                "Apply to All",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (response == JOptionPane.YES_OPTION) {
            saveCurrentAdjustment();
            ImageAdjustment current = adjustments.get(currentIndex);
            for (int i = 0; i < adjustments.size(); i++) {
                if (i != currentIndex) {
                    adjustments.set(i, new ImageAdjustment(current));
                }
            }
            JOptionPane.showMessageDialog(this, "Adjustments applied to all images.");
        }
    }

    private void applyAndClose() {
        saveCurrentAdjustment();
        applyAdjustments();
        dispose();
    }

    private void applyAdjustments() {
        // Save all adjustments
        saveCurrentAdjustment();

        // Apply adjustments to all images
        for (int i = 0; i < imageFiles.size(); i++) {
            File file = imageFiles.get(i);
            ImageAdjustment adjustment = adjustments.get(i);

            if (!adjustment.isDefault()) {
                try {
                    BufferedImage image = javax.imageio.ImageIO.read(Objects.requireNonNull(file));
                    BufferedImage adjusted = ImageUtils.applyAdjustmentsToImage(
                            image,
                            adjustment.getRotation(),
                            adjustment.isFlipHorizontal(),
                            adjustment.isFlipVertical(),
                            adjustment.getBrightness(),
                            adjustment.getContrast()
                    );

                    // Save the adjusted image back to the file
                    String format = file.getName().substring(file.getName().lastIndexOf('.') + 1);
                    javax.imageio.ImageIO.write(adjusted, format, file);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this,
                            "Error processing image: " + file.getName(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        JOptionPane.showMessageDialog(this,
                "All adjustments have been applied successfully.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
