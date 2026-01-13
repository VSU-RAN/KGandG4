package vsu.org.ran.kgandg4.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import vsu.org.ran.kgandg4.components.ColorPalette;
import vsu.org.ran.kgandg4.components.HueSlider;

import java.net.URL;
import java.util.ResourceBundle;

public class RenderPanelController implements Initializable {

    @FXML private ScrollPane scrollPane;
    @FXML private VBox renderPanel;

    @FXML private RadioButton solidModeRadio;
    @FXML private RadioButton textureModeRadio;
    @FXML private RadioButton lightingModeRadio;

    @FXML private ColorPicker faceColorPicker;
    @FXML private ColorPicker lightColorPicker;
    @FXML private Slider redSlider;
    @FXML private Slider greenSlider;
    @FXML private Slider blueSlider;
    @FXML private Slider lightIntensitySlider;
    @FXML private Label rgbLabel;

    @FXML private VBox paletteContainer;
    @FXML private Label redValue;
    @FXML private Label greenValue;
    @FXML private Label blueValue;
    @FXML private Label lightIntensityValue;

    // Новые чекбоксы
    @FXML private CheckBox showWireframeCheck;
    @FXML private CheckBox useTextureCheck;
    @FXML private CheckBox useLightingCheck;

    private GuiController guiController;
    private ToggleGroup renderGroup;

    // Пользовательские компоненты
    private ColorPalette colorPalette;
    private HueSlider hueSlider;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Настраиваем группу радио-кнопок
        renderGroup = new ToggleGroup();
        solidModeRadio.setToggleGroup(renderGroup);
        textureModeRadio.setToggleGroup(renderGroup);
        lightingModeRadio.setToggleGroup(renderGroup);
        solidModeRadio.setSelected(true);

        // Инициализируем ColorPicker с дефолтным цветом
        faceColorPicker.setValue(Color.web("#4a90e2"));

        // Инициализируем пользовательскую палитру
        initCustomPalette();

        // Назначаем обработчики
        setupEventHandlers();

        // Настраиваем слайдеры RGB
        setupRGBSliders();

        // Обновляем RGB label
        updateRGBLabel(
                (int)redSlider.getValue(),
                (int)greenSlider.getValue(),
                (int)blueSlider.getValue()
        );
    }

    private void initCustomPalette() {
        // Получаем размеры контейнера
        double containerWidth = paletteContainer.getWidth() - 16; // минус padding
        if (containerWidth <= 0) containerWidth = 280; // дефолтное значение

        // Создаем палитру цветов с правильными размерами
        colorPalette = new ColorPalette(containerWidth, 140);
        colorPalette.setSelectedColor(Color.web("#4a90e2"));

        // Создаем слайдер оттенка с правильными размерами
        hueSlider = new HueSlider(containerWidth, 20);
        hueSlider.setHue(Color.web("#4a90e2").getHue());

        // Связываем палитру и слайдер оттенка
        hueSlider.hueProperty().addListener((obs, oldVal, newVal) -> {
            colorPalette.setHue(newVal.doubleValue());
        });

        colorPalette.selectedColorProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Обновляем ColorPicker и слайдеры
                faceColorPicker.setValue(newVal);
                updateRGBFromColor(newVal);

                // Отправляем в GuiController
                if (guiController != null) {
                    guiController.setFaceColor(newVal);
                }
            }
        });

        // Очищаем контейнер и добавляем компоненты в правильном порядке
        paletteContainer.getChildren().clear();

        // Сначала добавляем лейбл
        Label label = new Label("Выберите цвет кликом");
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d; -fx-padding: 0 0 4 0;");
        paletteContainer.getChildren().add(label);

        // Затем слайдер
        paletteContainer.getChildren().add(hueSlider);

        // Затем палитру
        paletteContainer.getChildren().add(colorPalette);

        setupLightingControls();
    }

    public void setGuiController(GuiController guiController) {
        this.guiController = guiController;
    }

    public void updateSettings(String renderMode, Color faceColor) {
        // Устанавливаем режим рендеринга
        if (renderMode.equalsIgnoreCase("solid")) {
            solidModeRadio.setSelected(true);
        } else if (renderMode.equalsIgnoreCase("texture")) {
            textureModeRadio.setSelected(true);
        } else if (renderMode.equalsIgnoreCase("lighting")) { // Добавить
            lightingModeRadio.setSelected(true);
        }

        // Устанавливаем цвет
        if (faceColor != null) {
            faceColorPicker.setValue(faceColor);
            updateRGBFromColor(faceColor);

            // Обновляем пользовательскую палитру
            if (colorPalette != null) {
                colorPalette.setSelectedColor(faceColor);
            }
            if (hueSlider != null) {
                hueSlider.setHue(faceColor.getHue());
            }
        }
    }

    private void setupEventHandlers() {
        // При изменении радио-кнопок
        solidModeRadio.setOnAction(e -> {
            onRenderModeChanged();
        });

        textureModeRadio.setOnAction(e -> {
            onRenderModeChanged();
        });

        lightingModeRadio.setOnAction(e -> {
            onRenderModeChanged();
        });

        // При изменении цвета через ColorPicker
        faceColorPicker.setOnAction(e -> {
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

    private void setupLightingControls() {
        // Настройка слайдера интенсивности
        lightIntensitySlider.setMin(0.0);
        lightIntensitySlider.setMax(1.0);
        lightIntensitySlider.setValue(1.0);
        lightIntensitySlider.setBlockIncrement(0.1);

        // Обновляем значение
        updateLightIntensityLabel(1.0);

        // Обработчики
        lightIntensitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double intensity = Math.round(newVal.doubleValue() * 10.0) / 10.0;
            updateLightIntensityLabel(intensity);
            if (guiController != null) {
                guiController.setLightIntensity((float) intensity);
            }
        });

        lightColorPicker.setOnAction(e -> {
            Color color = lightColorPicker.getValue();
            if (guiController != null) {
                guiController.setLightColor(color);
            }
        });
    }

    private void updateLightIntensityLabel(double intensity) {
        lightIntensityValue.setText(String.format("%.1f", intensity));
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
            }else if (selected == lightingModeRadio) { // Добавить
                mode = "lighting";
            }
            guiController.setRenderMode(mode);
        }
    }

    private void onColorPickerChanged() {
        Color color = faceColorPicker.getValue();
        updateRGBFromColor(color);

        // Обновляем пользовательскую палитру
        if (colorPalette != null) {
            colorPalette.setSelectedColor(color);
        }
        if (hueSlider != null) {
            hueSlider.setHue(color.getHue());
        }

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
        updateRGBLabel(r, g, b);

        // Обновляем пользовательскую палитру
        if (colorPalette != null) {
            colorPalette.setSelectedColor(color);
        }
        if (hueSlider != null) {
            hueSlider.setHue(color.getHue());
        }

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

    public Parent getRenderPanel() {
        return scrollPane;
    }
}