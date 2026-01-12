package vsu.org.ran.kgandg4.components;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.event.EventHandler;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class ColorPalette extends Pane {
    private Canvas canvas;
    private DoubleProperty hue = new SimpleDoubleProperty(0); // 0-360
    private ObjectProperty<Color> selectedColor = new SimpleObjectProperty<>(Color.WHITE);

    public ColorPalette(double width, double height) {
        canvas = new Canvas(width, height);
        canvas.setWidth(width);
        canvas.setHeight(height);

        setPrefSize(width, height);
        setMaxSize(width, height);
        getChildren().add(canvas);

        // Нарисовать палитру при изменении размера
        widthProperty().addListener((obs, oldVal, newVal) -> {
            canvas.setWidth(newVal.doubleValue());
            drawPalette();
        });

        heightProperty().addListener((obs, oldVal, newVal) -> {
            canvas.setHeight(newVal.doubleValue());
            drawPalette();
        });

        // Нарисовать начальную палитру
        drawPalette();

        // Обработка мыши
        canvas.setOnMousePressed(event -> handleMouseClick(event.getX(), event.getY()));
        canvas.setOnMouseDragged(event -> handleMouseClick(event.getX(), event.getY()));
    }

    public ColorPalette() {
        this(200, 200);
    }

    private void drawPalette() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        // Очищаем
        gc.clearRect(0, 0, width, height);

        // Рисуем палитру насыщенности и яркости для текущего оттенка
        for (int x = 0; x < width; x++) {
            double saturation = x / width; // 0-1

            for (int y = 0; y < height; y++) {
                double brightness = 1.0 - (y / height); // 1-0 снизу вверх

                // Создаем цвет в HSV
                Color color = Color.hsb(hue.get(), saturation, brightness);
                gc.setFill(color);
                gc.fillRect(x, y, 1, 1);
            }
        }

        // Рисуем рамку
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(0, 0, width, height);
    }

    private void handleMouseClick(double x, double y) {
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        // Ограничиваем координаты
        if (x < 0) x = 0;
        if (x > width) x = width;
        if (y < 0) y = 0;
        if (y > height) y = height;

        // Рассчитываем насыщенность и яркость
        double saturation = x / width;
        double brightness = 1.0 - (y / height);

        // Создаем выбранный цвет
        Color newColor = Color.hsb(hue.get(), saturation, brightness);
        selectedColor.set(newColor);

        // Перерисовываем с маркером
        drawPaletteWithMarker(x, y);
    }

    private void drawPaletteWithMarker(double markerX, double markerY) {
        drawPalette();

        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Рисуем белый кружок
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeOval(markerX - 6, markerY - 6, 12, 12);

        // Рисуем черный кружок
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeOval(markerX - 6, markerY - 6, 12, 12);
    }

    // Геттеры и сеттеры свойств
    public double getHue() { return hue.get(); }
    public void setHue(double value) {
        hue.set(value);
        drawPalette();
    }
    public DoubleProperty hueProperty() { return hue; }

    public Color getSelectedColor() { return selectedColor.get(); }
    public void setSelectedColor(Color color) {
        selectedColor.set(color);
        // Обновляем hue из цвета
        hue.set(color.getHue());

        // Рассчитываем позицию маркера
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        double markerX = color.getSaturation() * width;
        double markerY = (1.0 - color.getBrightness()) * height;

        drawPaletteWithMarker(markerX, markerY);
    }
    public ObjectProperty<Color> selectedColorProperty() { return selectedColor; }
}