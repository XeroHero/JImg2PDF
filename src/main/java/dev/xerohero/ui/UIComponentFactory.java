package dev.xerohero.ui;

import dev.xerohero.theme.ThemeManager;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Factory class for creating styled UI components.
 */
public class UIComponentFactory {
    private static final UIComponentFactory instance = new UIComponentFactory();
    private final ThemeManager themeManager;
    
    private UIComponentFactory() {
        this.themeManager = ThemeManager.getInstance();
    }
    
    public static UIComponentFactory getInstance() {
        return instance;
    }
    
    public JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setBackground(UIManager.getColor("control"));
        button.setForeground(UIManager.getColor("textText"));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, 28));
        button.setMaximumSize(new Dimension(120, 28));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(button.getBackground().darker());
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(UIManager.getColor("control"));
            }
        });
        
        return button;
    }
    
    public JSlider createStyledSlider(int min, int max, int value, int majorTickSpacing) {
        JSlider slider = new JSlider(min, max, value);
        slider.setMajorTickSpacing(majorTickSpacing);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setOpaque(false);
        slider.setFocusable(false);
        slider.setPreferredSize(new Dimension(200, 50));
        return slider;
    }
    
    public JPanel createControlGroup(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        
        TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createEmptyBorder(),
            title,
            TitledBorder.LEFT,
            TitledBorder.TOP,
            panel.getFont().deriveFont(Font.BOLD)
        );
        
        if (themeManager.isDarkMode()) {
            border.setTitleColor(Color.WHITE);
        }
        
        panel.setBorder(border);
        return panel;
    }
    
    public void styleCheckbox(JCheckBox checkBox) {
        checkBox.setOpaque(false);
        checkBox.setFocusPainted(false);
        checkBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    
    public JSeparator createVerticalDivider() {
        JSeparator separator = new JSeparator(JSeparator.VERTICAL);
        separator.setPreferredSize(new Dimension(1, 1));
        return separator;
    }
    
    public JButton createThemeToggleButton() {
        JButton button = new JButton(ThemeManager.getInstance().isDarkMode() ? "☀️" : "🌙");
        styleThemeToggleButton(button);
        button.addActionListener(e -> {
            ThemeManager.getInstance().toggleTheme();
            button.setText(ThemeManager.getInstance().isDarkMode() ? "☀️" : "🌙");
            ThemeManager.getInstance().applyTheme(button.getTopLevelAncestor());
        });
        return button;
    }
    
    private void styleThemeToggleButton(JButton button) {
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setFont(button.getFont().deriveFont(24f));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setToolTipText("Toggle Dark Mode");
    }
}
