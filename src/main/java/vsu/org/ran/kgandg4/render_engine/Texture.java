package vsu.org.ran.kgandg4.render_engine;

import javafx.scene.image.*;
import javafx.scene.paint.Color;
import vsu.org.ran.kgandg4.IO.FileDialogService;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Autowired;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.PostConstruct;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Value;

import java.io.File;
import java.io.IOException;

@Component
public class Texture {
    private Image image;
    private PixelReader pixelReader;
    private int width;
    private int height;

    @Value("${texture.default}")
    private String defaultTexturePath;

    @Autowired
    private FileDialogService fileDialogService;


    public Texture() {
    }

    @PostConstruct
    public void init() throws IOException {
        File textureFile = fileDialogService.resolvePath(defaultTexturePath);

        if (textureFile.exists()) {
            this.image = new Image(textureFile.toURI().toString());
            if (!image.isError()) {
                this.pixelReader = image.getPixelReader();
                this.width = (int) image.getWidth();
                this.height = (int) image.getHeight();
            }
        }
    }

    public Color getColor(float u, float v) {
        v = 1.0f - v;
        u = 1.0f - u;

        int x = (int)(u * width) % width;
        int y = (int)(v * height) % height;

        if (x < 0) x += width;
        if (y < 0) y += height;

        return pixelReader.getColor(x, y);
    }
}