package vsu.org.ran.kgandg4.render_engine;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import javafx.scene.image.*;
import javafx.scene.paint.Color;

import vsu.org.ran.kgandg4.IO.FileDialogService;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Value;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Autowired;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.PostConstruct;




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

    public CompletableFuture<Void> loadFromFile(String filePath) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        try {
            this.image = new Image(filePath, true);


            if (image.isError()) {
                Throwable error = image.getException();
                future.completeExceptionally(new IOException("Ошибка загрузки изображения", error));
                return future;
            }

            image.progressProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() == 1.0) {
                    if (image.isError()) {
                        Throwable error = image.getException();
                        future.completeExceptionally(new IOException("Ошибка загрузки изображения", error));
                    } else {
                        this.pixelReader = image.getPixelReader();
                        future.complete(null);
                    }
                }
            });

            if (!image.isBackgroundLoading() || image.getProgress() == 1.0) {
                if (image.isError()) {
                    Throwable error = image.getException();
                    future.completeExceptionally(new IOException("Ошибка загрузки изображения", error));
                } else {
                    this.pixelReader = image.getPixelReader();
                    future.complete(null);
                }
            }

        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    public void setMaterialColor(Color color) {
        this.materialColor = color;
    }

    public void enableTexture(boolean enable) throws IllegalStateException {
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