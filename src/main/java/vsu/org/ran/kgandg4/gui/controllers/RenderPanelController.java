package vsu.org.ran.kgandg4.gui.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import vsu.org.ran.kgandg4.gui.PanelController;
import vsu.org.ran.kgandg4.gui.components.HueSlider;
import vsu.org.ran.kgandg4.render_engine.render.Scene;
import vsu.org.ran.kgandg4.gui.components.ColorPalette;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Autowired;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;

@Component
public class RenderPanelController implements Initializable, PanelController {

    @FXML private ScrollPane scrollPane;

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
    @FXML private Slider ambientStrengthSlider;
    @FXML private Label ambientStrengthValue;

    @FXML private CheckBox wireframeOnlyCheck;
    @FXML private CheckBox showWireframeCheck;
    @FXML private CheckBox useTextureCheck;
    @FXML private CheckBox useLightingCheck;

    // Пользовательские компоненты
    private ColorPalette colorPalette;
    private HueSlider hueSlider;

    @Autowired
    private Scene scene;

    @Autowired
    private AlertService alertService;

    private boolean updatingColor = false;
    private boolean updatingCheckboxes = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Настраиваем чекбоксы
        initCheckboxes();

        // 2. Настраиваем цвет материала
        initMaterialColorControls();

        // 3. Настраиваем освещение
        initLightingControls();

        // 4. Настраиваем пользовательскую палитру
        initCustomPalette();
    }

    private void initCheckboxes() {
        if (scene != null) {
            wireframeOnlyCheck.setSelected(scene.isWireframeOnlyEnabled());
            showWireframeCheck.setSelected(scene.isWireframeEnabled());
            useTextureCheck.setSelected(scene.isTextureEnabled());
            useLightingCheck.setSelected(scene.isLightEnabled());
        }

        showWireframeCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (updatingCheckboxes) return;

            updatingCheckboxes = true;
            try {
                if (scene != null) {
                    if (newVal && wireframeOnlyCheck.isSelected()) {
                        wireframeOnlyCheck.setSelected(false);
                        scene.setWireframeOnlyEnabled(false);
                    }
                    scene.setWireframeEnabled(newVal);
                }
            } finally {
                updatingCheckboxes = false;
            }
        });

        useTextureCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (updatingCheckboxes) return;

            updatingCheckboxes = true;
            try {
                if (scene != null) {
                    if (newVal && wireframeOnlyCheck.isSelected()) {
                        wireframeOnlyCheck.setSelected(false);
                        scene.setWireframeOnlyEnabled(false);
                    }
                    try {
                        scene.setTextureEnabled(newVal);
                    } catch (IllegalStateException e) {
                        useTextureCheck.setSelected(oldVal);
                        alertService.showError("Текстура не загружена", e.getMessage() + "\n\nПожалуйста, сначала загрузите текстуру в панели редактирования модели.");
                    }
                }
            } finally {
                updatingCheckboxes = false;
            }
        });

        useLightingCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            updatingCheckboxes = true;
            try {
                if (scene != null) {
                    if (newVal && wireframeOnlyCheck.isSelected()) {
                        wireframeOnlyCheck.setSelected(false);
                        scene.setWireframeOnlyEnabled(false);
                    }
                    scene.setLightEnabled(newVal);
                }
            } finally {
                updatingCheckboxes = false;
            }
        });

        wireframeOnlyCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (updatingCheckboxes) return;

            updatingCheckboxes = true;
            try {
                if (scene != null) {
                    if (newVal) {
                        // Отключаем все остальные режимы
                        showWireframeCheck.setSelected(false);
                        useTextureCheck.setSelected(false);
                        useLightingCheck.setSelected(false);

                        // Устанавливаем режим в сцене
                        scene.setWireframeEnabled(false);
                        scene.setTextureEnabled(false);
                        scene.setLightEnabled(false);
                        scene.setWireframeOnlyEnabled(true);
                    } else {
                        // При выключении "только каркас" включаем базовый каркас
                        showWireframeCheck.setSelected(true);
                        scene.setWireframeEnabled(true);
                        scene.setWireframeOnlyEnabled(false);
                    }
                }
            } finally {
                updatingCheckboxes = false;
            }
        });
    }

    private void updateCheckboxesFromContext() {
        if (scene == null) return;
        updatingCheckboxes = true;
        try {
            showWireframeCheck.setSelected(scene.isWireframeEnabled());
            wireframeOnlyCheck.setSelected(scene.isWireframeOnlyEnabled());
            useTextureCheck.setSelected(scene.isTextureEnabled());
            useLightingCheck.setSelected(scene.isLightEnabled());
        } finally {
            updatingCheckboxes = false;
        }
    }



    private void initMaterialColorControls() {
        // Начальный цвет из RenderContext
        if (scene != null) {
            Color materialColor = scene.getMaterialColor();
            faceColorPicker.setValue(materialColor);
            updateRGBFromColor(materialColor);
        }

        // Обработчик ColorPicker
        faceColorPicker.setOnAction(e -> {
            if (!updatingColor) {
                updatingColor = true;
                try {
                    Color color = faceColorPicker.getValue();
                    updateRGBFromColor(color);

                    // Обновляем пользовательскую палитру
                    if (colorPalette != null) {
                        colorPalette.setSelectedColor(color);
                    }
                    if (hueSlider != null) {
                        hueSlider.setHue(color.getHue());
                    }

                    // Сохраняем цвет в RenderContext
                    scene.setMaterialColor(color);
                } finally {
                    updatingColor = false;
                }
            }
        });
    }

    private void initLightingControls() {
        // Настройка слайдера интенсивности освещения
        lightIntensitySlider.setMin(0.0);
        lightIntensitySlider.setMax(100.0);
        ambientStrengthSlider.setMin(0.0);
        ambientStrengthSlider.setMax(100.0);

        if (scene != null && scene.getLightning() != null) {
            lightIntensitySlider.setValue(scene.getLightIntensity() * 100.0f);
            ambientStrengthSlider.setValue(scene.getAmbientStrength() * 100.0f);
        }

        // Обновляем лейбл
        updateLightIntensityLabel(lightIntensitySlider.getValue());
        updateAmbientStrengthLabel(ambientStrengthSlider.getValue());

        // Обработчик слайдера интенсивности
        lightIntensitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double intensity = Math.round(newVal.doubleValue());
            updateLightIntensityLabel(intensity);

            scene.setLightIntensity((float) (intensity / 100));
        });

        ambientStrengthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double strength = Math.round(newVal.doubleValue());
            updateAmbientStrengthLabel(strength);

            scene.setAmbientStrength((float) (strength / 100.0));
        });

        // Настройка ColorPicker для цвета света
        if (scene != null) {
            lightColorPicker.setValue(scene.getLightColor());
        }

        // Обработчик цвета света
        lightColorPicker.setOnAction(e -> {
            Color color = lightColorPicker.getValue();
            if (scene != null) {
                scene.setLightColor(color);
            }
        });
    }

    private void initCustomPalette() {
        // Получаем размеры контейнера
        double containerWidth = paletteContainer.getWidth() - 16; // минус padding
        if (containerWidth <= 0) containerWidth = 280; // дефолтное значение

        // Создаем палитру цветов
        colorPalette = new ColorPalette(containerWidth, 140);

        // Создаем слайдер оттенка
        hueSlider = new HueSlider(containerWidth, 20);

        // Устанавливаем начальный цвет
        Color initialColor = scene.getMaterialColor();

        colorPalette.setSelectedColor(initialColor);
        hueSlider.setHue(initialColor.getHue());

        // Связываем палитру и слайдер оттенка
        hueSlider.hueProperty().addListener((obs, oldVal, newVal) -> {
            colorPalette.setHue(newVal.doubleValue());
        });

        // Обработчик выбора цвета в палитре
        colorPalette.selectedColorProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !updatingColor) {
                updatingColor = true;
                try {
                    // Обновляем ColorPicker и слайдеры
                    faceColorPicker.setValue(newVal);
                    updateRGBFromColor(newVal);

                    // Сохраняем цвет в RenderContext
                    if (scene != null) {
                        scene.setMaterialColor(newVal);
                    }
                } finally {
                    updatingColor = false;
                }
            }
        });

        // Очищаем контейнер и добавляем компоненты
        paletteContainer.getChildren().clear();

        // Добавляем лейбл
        Label label = new Label("Выберите цвет материала");
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d; -fx-padding: 0 0 4 0;");
        paletteContainer.getChildren().add(label);

        // Добавляем слайдер
        paletteContainer.getChildren().add(hueSlider);

        // Добавляем палитру
        paletteContainer.getChildren().add(colorPalette);

        // Настраиваем RGB слайдеры
        setupRGBSliders();
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

    private void onRGBSliderChanged() {
        if (updatingColor) return;

        updatingColor = true;
        try {
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

            // Сохраняем цвет в RenderContext
            if (scene != null) {
                scene.setMaterialColor(color);
            }
        } finally {
            updatingColor = false;
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

    private void updateRGBLabels() {
        redValue.setText(String.valueOf((int)redSlider.getValue()));
        greenValue.setText(String.valueOf((int)greenSlider.getValue()));
        blueValue.setText(String.valueOf((int)blueSlider.getValue()));
    }

    private void updateRGBLabel(int r, int g, int b) {
        rgbLabel.setText(String.format("RGB: %d, %d, %d", r, g, b));
    }

    private void updateLightIntensityLabel(double intensity) {
        lightIntensityValue.setText(String.format("%.0f%%", intensity));
    }

    private void updateAmbientStrengthLabel(double strength) {
        ambientStrengthValue.setText(String.format("%.0f%%", strength));
    }

    @Override
    public void onPanelShow() {
        refresh();
    }

    private void refresh() {
        if (scene == null) return;
        updateCheckboxesFromContext();

        Color materialColor = scene.getMaterialColor();
        if (!updatingColor) {
            updatingColor = true;
            try {
                faceColorPicker.setValue(materialColor);
                updateRGBFromColor(materialColor);

                if (colorPalette != null) {
                    colorPalette.setSelectedColor(materialColor);
                }
                if (hueSlider != null) {
                    hueSlider.setHue(materialColor.getHue());
                }
            } finally {
                updatingColor = false;
            }
        }

        lightIntensitySlider.setValue(scene.getLightIntensity() * 100.0f);
        updateLightIntensityLabel(scene.getLightIntensity() * 100.0f);

        ambientStrengthSlider.setValue(scene.getAmbientStrength() * 100.0f);
        updateAmbientStrengthLabel(scene.getAmbientStrength() * 100.0f);

        lightColorPicker.setValue(scene.getLightColor());
    }
}