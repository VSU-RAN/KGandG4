package vsu.org.ran.kgandg4.render_engine;

import javafx.scene.image.*;
import javafx.scene.paint.Color;
import java.io.IOException;

public class Texture {
    private final Image image;
    private final PixelReader pixelReader;
    private final int width;
    private final int height;

    public Texture(String path) throws IOException {
        this.image = new Image("file:" + path);
        this.pixelReader = image.getPixelReader();
        this.width = (int) image.getWidth();
        this.height = (int) image.getHeight();
    }

    public Color getColor(float u, float v) {
        int x = (int)(u * width) % width;
        int y = (int)(v * height) % height;

        if (x < 0) x += width;
        if (y < 0) y += height;

        return pixelReader.getColor(x, y);
    }

    public Image getImage() { return image; }
}