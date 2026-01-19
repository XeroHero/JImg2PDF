package dev.xerohero;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageToPdfGui extends JFrame {
    private final DefaultListModel<String> listModel;
    private final JList<String> fileList;
    private File outputFile;

    public ImageToPdfGui() {
        setTitle("Image to PDF Converter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);  // Increased window size
        setMinimumSize(new Dimension(700, 500));  // Set minimum size
        setLocationRelativeTo(null);
        
        // Main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Top panel for buttons with multiple rows if needed
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.weightx = 0;
        
        // First row of buttons
        JButton addButton = new JButton("Add Images");
        JButton removeButton = new JButton("Remove");
        JButton clearButton = new JButton("Clear All");
        JButton upButton = new JButton("Move Up");
        JButton downButton = new JButton("Move Down");
        
        // Adjust Images button
        JButton adjustButton = new JButton("🛠️ Adjust Images");
        adjustButton.setBackground(new Color(240, 245, 255));  // Lighter blue
        adjustButton.setToolTipText("Adjust selected images (rotate, flip, brightness, contrast)");
        
        // Add first row of buttons
        int x = 0;
        buttonPanel.add(addButton, gbc);
        gbc.gridx = ++x;
        buttonPanel.add(removeButton, gbc);
        gbc.gridx = ++x;
        buttonPanel.add(clearButton, gbc);
        gbc.gridx = ++x;
        buttonPanel.add(Box.createHorizontalStrut(10), gbc);
        gbc.gridx = ++x;
        buttonPanel.add(upButton, gbc);
        gbc.gridx = ++x;
        buttonPanel.add(downButton, gbc);
        
        // Add some space
        gbc.gridx = ++x;
        buttonPanel.add(Box.createHorizontalStrut(20), gbc);
        
        // Add the Adjust Images button
        gbc.gridx = ++x;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        buttonPanel.add(adjustButton, gbc);
        
        // Add some padding at the bottom of the button panel
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = x + 2;
        gbc.weighty = 1.0;
        buttonPanel.add(Box.createVerticalStrut(10), gbc);
        
        // Set the preferred size to ensure the button is fully visible
        adjustButton.setPreferredSize(new Dimension(140, 28));
        
        // Make all buttons the same size
        Dimension buttonSize = new Dimension(120, 28);
        addButton.setPreferredSize(buttonSize);
        removeButton.setPreferredSize(buttonSize);
        clearButton.setPreferredSize(buttonSize);
        upButton.setPreferredSize(buttonSize);
        downButton.setPreferredSize(buttonSize);
        
        // Add some padding around the button panel
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // File list
        listModel = new DefaultListModel<>();
        fileList = new JList<>(listModel);
        fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollPane = new JScrollPane(fileList);
        
        // Bottom panel for output file selection and convert button
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        
        JPanel outputPanel = new JPanel(new BorderLayout(5, 5));
        JLabel outputLabel = new JLabel("Output PDF:");
        JTextField outputField = new JTextField();
        outputField.setEditable(false);
        JButton browseButton = new JButton("Browse...");
        
        outputPanel.add(outputLabel, BorderLayout.WEST);
        outputPanel.add(outputField, BorderLayout.CENTER);
        outputPanel.add(browseButton, BorderLayout.EAST);
        
        JButton convertButton = new JButton("Convert to PDF");
        convertButton.setFont(convertButton.getFont().deriveFont(Font.BOLD, 14f));
        
        bottomPanel.add(outputPanel, BorderLayout.CENTER);
        bottomPanel.add(convertButton, BorderLayout.SOUTH);
        
        // Add components to main panel
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Add action listeners
        addButton.addActionListener(e -> addImages());
        removeButton.addActionListener(e -> removeSelected());
        clearButton.addActionListener(e -> listModel.clear());
        upButton.addActionListener(e -> moveItem(-1));
        downButton.addActionListener(e -> moveItem(1));
        
        // Add action listener for adjust button
        adjustButton.addActionListener(e -> {
            if (listModel.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please add some images first.", 
                    "No Images", 
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Get the list of image files
            List<File> imageFiles = new ArrayList<>();
            for (int i = 0; i < listModel.size(); i++) {
                imageFiles.add(new File(listModel.getElementAt(i)));
            }
            
            // Show the adjustment dialog
            ImageAdjustmentDialog dialog = new ImageAdjustmentDialog(this, imageFiles);
            dialog.setVisible(true);
        });
        
        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save PDF As");
            fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));
            fileChooser.setSelectedFile(new File("output.pdf"));
            
            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                outputFile = fileChooser.getSelectedFile();
                if (!outputFile.getName().toLowerCase().endsWith(".pdf")) {
                    outputFile = new File(outputFile.getAbsolutePath() + ".pdf");
                }
                outputField.setText(outputFile.getAbsolutePath());
            }
        });
        
        convertButton.addActionListener(e -> convertToPdf());
        
        // Add main panel to frame
        add(mainPanel);
    }
    
    private void addImages() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Images");
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(new FileNameExtensionFilter(
            "Image files", "jpg", "jpeg", "png", "gif", "bmp", "tif", "tiff"
        ));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            for (File file : fileChooser.getSelectedFiles()) {
                listModel.addElement(file.getAbsolutePath());
            }
        }
    }
    
    private void removeSelected() {
        int[] selectedIndices = fileList.getSelectedIndices();
        for (int i = selectedIndices.length - 1; i >= 0; i--) {
            listModel.remove(selectedIndices[i]);
        }
    }
    
    private void moveItem(int direction) {
        int selectedIndex = fileList.getSelectedIndex();
        if (selectedIndex == -1 || 
            (direction < 0 && selectedIndex == 0) || 
            (direction > 0 && selectedIndex == listModel.getSize() - 1)) {
            return;
        }
        
        int newIndex = selectedIndex + direction;
        String item = listModel.getElementAt(selectedIndex);
        listModel.remove(selectedIndex);
        listModel.add(newIndex, item);
        fileList.setSelectedIndex(newIndex);
    }
    
    private void convertToPdf() {
        if (listModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please add at least one image to convert.", 
                "No Images", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (outputFile == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select an output PDF file.", 
                "No Output File", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        List<File> imageFiles = new ArrayList<>();
        for (int i = 0; i < listModel.size(); i++) {
            imageFiles.add(new File(listModel.getElementAt(i)));
        }
        
        try {
            ImageToPdfConverter.convertImagesToPdf(imageFiles, outputFile.getAbsolutePath());
            JOptionPane.showMessageDialog(this, 
                "PDF created successfully!\n" + outputFile.getAbsolutePath(), 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error creating PDF: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        // Launch the application
        SwingUtilities.invokeLater(() -> {
            ImageToPdfGui gui = new ImageToPdfGui();
            gui.setVisible(true);
        });
    }
}
