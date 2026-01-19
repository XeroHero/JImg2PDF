package dev.xerohero.model;

/**
 * Represents the adjustment settings for an image.
 */
public class ImageAdjustment {
    private int rotation = 0;
    private boolean flipHorizontal = false;
    private boolean flipVertical = false;
    private int brightness = 0;
    private int contrast = 0;

    public ImageAdjustment() {}

    public ImageAdjustment(ImageAdjustment other) {
        this.rotation = other.rotation;
        this.flipHorizontal = other.flipHorizontal;
        this.flipVertical = other.flipVertical;
        this.brightness = other.brightness;
        this.contrast = other.contrast;
    }

    // Getters and Setters
    public int getRotation() { return rotation; }
    public void setRotation(int rotation) { this.rotation = rotation; }
    
    public boolean isFlipHorizontal() { return flipHorizontal; }
    public void setFlipHorizontal(boolean flipHorizontal) { this.flipHorizontal = flipHorizontal; }
    
    public boolean isFlipVertical() { return flipVertical; }
    public void setFlipVertical(boolean flipVertical) { this.flipVertical = flipVertical; }
    
    public int getBrightness() { return brightness; }
    public void setBrightness(int brightness) { this.brightness = brightness; }
    
    public int getContrast() { return contrast; }
    public void setContrast(int contrast) { this.contrast = contrast; }

    public boolean isDefault() {
        return rotation == 0 && !flipHorizontal && !flipVertical && 
               brightness == 0 && contrast == 0;
    }
}
