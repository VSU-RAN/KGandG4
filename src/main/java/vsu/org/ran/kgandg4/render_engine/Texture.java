package vsu.org.ran.kgandg4.render_engine;

import javafx.scene.image.*;
import javafx.scene.paint.Color;
import vsu.org.ran.kgandg4.IO.FileDialogService;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Autowired;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.PostConstruct;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Value;

import java.io.IOException;

@Component
public class Texture {
    private Image image;
    private PixelReader pixelReader;

    @Value("${texture.default.color}")
    private String defaultColorString;
    private Color materialColor;

    private boolean useTexture = false;

    @Autowired
    private FileDialogService fileDialogService;


    public Texture() {
    }

    @PostConstruct
    public void init(){
        this.materialColor = Color.web(defaultColorString);
    }

    public void loadFromFile(String filePath) {
        try {
            this.image = new Image(filePath, true);


            if (image.isError()) {
                throw new IOException(image.getException() != null ? image.getException().getMessage() : "Ошибка загрузки изображения");
            }

            image.progressProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() == 1.0 && !image.isError()) {
                    this.pixelReader = image.getPixelReader();
                }
            });

        } catch (IOException e) {
            throw new RuntimeException("Не удалось загрузить текстуру: " + e.getMessage(), e);
        }
    }

    public void setMaterialColor(Color color) {
        this.materialColor = color;
    }

    public void enableTexture(boolean enable) {
        if (enable && !hasTexture()) {
            throw new IllegalStateException("Нельзя включить режим отрисовки с текстурой: текстура не загружена");
        }
        this.useTexture = enable;
    }

    public Color getColor(float u, float v) {
        if (useTexture && hasTexture()) {
            return getTextureColor(u, v);
        }
        return materialColor;
    }

    public Color getTextureColor(float u, float v) {
        if (!hasTexture()) {
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
        return this.image != null && this.pixelReader != null;
    }

    public Image getTexture() {
        return image;
    }

    public Color getMaterialColor() {
        return materialColor;
    }

    @Override
    public String toString() {
        return String.format("Текстура: %.0fx%.0f", this.image.getWidth(), this.image.getHeight());
    }
}