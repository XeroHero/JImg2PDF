package dev.xerohero.theme;

import javax.swing.*;
import java.awt.*;

/**
 * Manages the application's theme (light/dark mode).
 */
public class ThemeManager {
    private static final Color DARK_BG = new Color(45, 45, 45);
    private static final Color DARK_FG = new Color(220, 220, 220);
    private static final Color DARK_PANEL = new Color(60, 63, 65);
    
    private boolean darkMode = false;
    private static ThemeManager instance;
    
    private ThemeManager() {}
    
    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }
    
    public boolean isDarkMode() {
        return darkMode;
    }
    
    public void toggleTheme() {
        darkMode = !darkMode;
    }
    
    public void applyTheme(Component component) {
        if (darkMode) {
            applyDarkTheme(component);
        } else {
            applyLightTheme(component);
        }
    }
    
    private void applyDarkTheme(Component component) {
        // Update UIManager for default colors
        UIManager.put("Panel.background", DARK_BG);
        UIManager.put("Label.foreground", DARK_FG);
        UIManager.put("Button.foreground", DARK_FG);
        UIManager.put("Button.background", DARK_PANEL);
        UIManager.put("CheckBox.foreground", DARK_FG);
        UIManager.put("Slider.foreground", DARK_FG);
        UIManager.put("Slider.background", DARK_BG);
        
        updateComponentTreeUI(component);
    }
    
    private void applyLightTheme(Component component) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Reset default colors
            Color bg = UIManager.getColor("Panel.background");
            Color fg = UIManager.getColor("Label.foreground");
            
            UIManager.put("Panel.background", bg);
            UIManager.put("Label.foreground", fg);
            UIManager.put("Button.foreground", fg);
            UIManager.put("Button.background", null);
            UIManager.put("CheckBox.foreground", fg);
            UIManager.put("Slider.foreground", fg);
            UIManager.put("Slider.background", bg);
            
            updateComponentTreeUI(component);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void updateComponentTreeUI(Component component) {
        if (component instanceof javax.swing.JComponent) {
            javax.swing.SwingUtilities.updateComponentTreeUI(component);
        }
        if (component instanceof java.awt.Container) {
            for (java.awt.Component child : ((java.awt.Container) component).getComponents()) {
                updateComponentTreeUI(child);
            }
        }
    }
}
