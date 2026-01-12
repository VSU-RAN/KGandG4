package vsu.org.ran.kgandg4.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import vsu.org.ran.kgandg4.GuiController;

import java.net.URL;
import java.util.ResourceBundle;

public class RenderPanelController implements Initializable {

    @FXML private VBox renderPanel;

    @FXML private RadioButton solidModeRadio;
    @FXML private RadioButton textureModeRadio;

    @FXML private ColorPicker faceColorPicker;
    @FXML private Slider redSlider;
    @FXML private Slider greenSlider;
    @FXML private Slider blueSlider;
    @FXML private Label rgbLabel;

    @FXML private ImageView colorPreview;

    @FXML private Label redValue;
    @FXML private Label greenValue;
    @FXML private Label blueValue;

    private GuiController guiController;
    private ToggleGroup renderGroup;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Инициализация RenderPanelController...");

        // Настраиваем группу радио-кнопок
        renderGroup = new ToggleGroup();
        solidModeRadio.setToggleGroup(renderGroup);
        textureModeRadio.setToggleGroup(renderGroup);
        solidModeRadio.setSelected(true);

        // ИНИЦИАЛИЗИРУЕМ ColorPicker с дефолтным цветом
        // Вместо того чтобы полагаться на значение из FXML
        faceColorPicker.setValue(Color.web("#4a90e2"));

        // Назначаем обработчики
        setupEventHandlers();

        // Настраиваем слайдеры RGB
        setupRGBSliders();

        updateColorPreview();

        System.out.println("✓ RenderPanelController инициализирован");
    }

    public void setGuiController(GuiController guiController) {
        this.guiController = guiController;
        System.out.println("✓ GuiController установлен в RenderPanelController");
    }

    public void updateSettings(String renderMode, Color faceColor) {
        System.out.println("Обновление настроек в RenderPanelController: mode=" + renderMode + ", color=" + faceColor);

        // Устанавливаем режим рендеринга
        if (renderMode.equalsIgnoreCase("solid")) {
            solidModeRadio.setSelected(true);
        } else if (renderMode.equalsIgnoreCase("texture")) {
            textureModeRadio.setSelected(true);
        }

        // Устанавливаем цвет
        if (faceColor != null) {
            faceColorPicker.setValue(faceColor);
            updateRGBFromColor(faceColor);
            updateColorPreview();
        }
    }

    private void setupEventHandlers() {
        System.out.println("Настройка обработчиков событий...");

        // При изменении радио-кнопок
        solidModeRadio.setOnAction(e -> {
            System.out.println("Выбран режим: Заливка цветом");
            onRenderModeChanged();
        });

        textureModeRadio.setOnAction(e -> {
            System.out.println("Выбран режим: Текстура");
            onRenderModeChanged();
        });

        // При изменении цвета через ColorPicker
        faceColorPicker.setOnAction(e -> {
            System.out.println("Цвет изменен через ColorPicker: " + faceColorPicker.getValue());
            onColorPickerChanged();
        });
    }

    private void setupRGBSliders() {
        // Настраиваем слайдеры RGB
        redSlider.setMin(0);
        redSlider.setMax(255);
        redSlider.setValue(74);
        redSlider.setBlockIncrement(1);

        greenSlider.setMin(0);
        greenSlider.setMax(255);
        greenSlider.setValue(144);
        greenSlider.setBlockIncrement(1);

        blueSlider.setMin(0);
        blueSlider.setMax(255);
        blueSlider.setValue(226);
        blueSlider.setBlockIncrement(1);

        // Обновляем значения лейблов
        updateRGBLabels();

        // Обработчики для слайдеров
        redSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            redValue.setText(String.valueOf(newVal.intValue()));
            onRGBSliderChanged();
        });

        greenSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            greenValue.setText(String.valueOf(newVal.intValue()));
            onRGBSliderChanged();
        });

        blueSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            blueValue.setText(String.valueOf(newVal.intValue()));
            onRGBSliderChanged();
        });
    }

    private void updateRGBLabels() {
        redValue.setText(String.valueOf((int)redSlider.getValue()));
        greenValue.setText(String.valueOf((int)greenSlider.getValue()));
        blueValue.setText(String.valueOf((int)blueSlider.getValue()));
    }

    private void onRenderModeChanged() {
        RadioButton selected = (RadioButton) renderGroup.getSelectedToggle();
        if (selected != null && guiController != null) {
            String mode = "";
            if (selected == solidModeRadio) {
                mode = "solid";
            } else if (selected == textureModeRadio) {
                mode = "texture";
            }
            System.out.println("Установка режима рендеринга: " + mode);
            guiController.setRenderMode(mode);
        }
    }

    private void onColorPickerChanged() {
        Color color = faceColorPicker.getValue();
        updateRGBFromColor(color);
        updateColorPreview();

        if (guiController != null) {
            guiController.setFaceColor(color);
        }
    }

    private void onRGBSliderChanged() {
        int r = (int) redSlider.getValue();
        int g = (int) greenSlider.getValue();
        int b = (int) blueSlider.getValue();

        Color color = Color.rgb(r, g, b);
        faceColorPicker.setValue(color);
        updateColorPreview();
        updateRGBLabel(r, g, b);

        if (guiController != null) {
            guiController.setFaceColor(color);
        }
    }

    private void updateRGBFromColor(Color color) {
        redSlider.setValue(color.getRed() * 255);
        greenSlider.setValue(color.getGreen() * 255);
        blueSlider.setValue(color.getBlue() * 255);
        updateRGBLabel(
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255)
        );
        updateRGBLabels();
    }

    private void updateRGBLabel(int r, int g, int b) {
        rgbLabel.setText(String.format("RGB: %d, %d, %d", r, g, b));
    }

    private void updateColorPreview() {
        Color color = faceColorPicker.getValue();
        if (color == null) {
            color = Color.web("#4a90e2"); // Дефолтный цвет
        }

        String hex = String.format("#%02X%02X%02X",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255)
        );

        colorPreview.setStyle(String.format(
                "-fx-background-color: %s; -fx-border-color: #000000; -fx-border-width: 1;",
                hex
        ));
    }

    public VBox getRenderPanel() {
        return renderPanel;
    }
}