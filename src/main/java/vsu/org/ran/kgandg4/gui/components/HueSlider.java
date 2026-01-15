package vsu.org.ran.kgandg4.gui.components;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class HueSlider extends Pane {
    private Canvas canvas;
    private DoubleProperty hue = new SimpleDoubleProperty(0); // 0-360

    public HueSlider(double width, double height) {
        canvas = new Canvas(width, height);
        canvas.setWidth(width);
        canvas.setHeight(height);

        setPrefSize(width, height);
        setMaxSize(width, height);
        getChildren().add(canvas);

        // Нарисовать слайдер
        drawSlider();

        // Обработка мыши
        canvas.setOnMousePressed(event -> handleMouseClick(event.getX()));
        canvas.setOnMouseDragged(event -> handleMouseClick(event.getX()));
    }

    public HueSlider() {
        this(200, 20);
    }

    private void drawSlider() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        // Очищаем
        gc.clearRect(0, 0, width, height);

        // Рисуем градиент оттенков
        for (int x = 0; x < width; x++) {
            double hueValue = (x / width) * 360;
            Color color = Color.hsb(hueValue, 1.0, 1.0);
            gc.setFill(color);
            gc.fillRect(x, 0, 1, height);
        }

        // Рисуем рамку
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(0, 0, width, height);

        // Рисуем маркер
        drawMarker();
    }

    private void drawMarker() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        // Позиция маркера
        double markerX = (hue.get() / 360.0) * width;

        // Рисуем треугольный маркер
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);

        // Треугольник вверху
        double[] xPoints = {markerX - 5, markerX + 5, markerX};
        double[] yPoints = {0, 0, 8};
        gc.fillPolygon(xPoints, yPoints, 3);
        gc.strokePolygon(xPoints, yPoints, 3);

        // Треугольник внизу
        yPoints = new double[]{height, height, height - 8};
        gc.fillPolygon(xPoints, yPoints, 3);
        gc.strokePolygon(xPoints, yPoints, 3);
    }

    private void handleMouseClick(double x) {
        double width = canvas.getWidth();

        // Ограничиваем координаты
        if (x < 0) x = 0;
        if (x > width) x = width;

        // Рассчитываем hue
        double newHue = (x / width) * 360;
        hue.set(newHue);

        // Перерисовываем
        drawSlider();
    }

    // Геттеры и сеттеры
    public double getHue() { return hue.get(); }
    public void setHue(double value) {
        hue.set(value);
        drawSlider();
    }
    public DoubleProperty hueProperty() { return hue; }
}