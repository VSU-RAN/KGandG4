package vsu.org.ran.kgandg4.render_engine;


import javafx.scene.image.*;
import javafx.scene.paint.Color;


public class Texture {
    private Image image;
    private PixelReader pixelReader;
    private Color materialColor;
    private boolean textureLoaded = false;


    public Texture() {
        this(Color.GRAY);
    }

    public Texture(Color color) {
        this.materialColor = color;
    }

    public Texture(Texture other) {
        this.image = other.image;
        this.pixelReader = other.pixelReader;
        this.materialColor = other.materialColor;
        this.textureLoaded = other.textureLoaded;
    }

    public void loadFromImage(Image newImage) {
        if (newImage != null && !newImage.isError()) {
            this.image = newImage;
            this.pixelReader = newImage.getPixelReader();
            this.textureLoaded = true;
        }
    }

    public void setMaterialColor(Color color) {
        this.materialColor = color;
    }

    public Color getColor(float u, float v, boolean useTexture) {
        if (useTexture && this.hasTexture()) {
            return getTextureColor(u, v);
        }
        return materialColor;
    }

    public Color getTextureColor(float u, float v) {
        if (!this.hasTexture()) {
            return materialColor;
        }

        v = 1.0f - v;
        u = 1.0f - u;


        int width = (int) image.getWidth();
        int height = (int) image.getHeight();


        int x = (int)(u * width) % width;
        int y = (int)(v * height) % height;

        if (x < 0) x += width;
        if (y < 0) y += height;

        return pixelReader.getColor(x, y);
    }

    public boolean hasTexture() {
        return textureLoaded && image != null;
    }

    public Image getTexture() {
        return image;
    }

    public Color getMaterialColor() {
        return materialColor;
    }

    public Texture copy() {
        return new Texture(this);
    }

    @Override
    public String toString() {
        return String.format("Текстура: %.0fx%.0f", this.image.getWidth(), this.image.getHeight());
    }
}