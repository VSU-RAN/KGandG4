package vsu.org.ran.kgandg4.gui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Autowired;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Value;
import vsu.org.ran.kgandg4.gui.components.ColorPalette;
import vsu.org.ran.kgandg4.gui.components.HueSlider;
import vsu.org.ran.kgandg4.gui.MainController;
import vsu.org.ran.kgandg4.gui.PanelController;
import vsu.org.ran.kgandg4.render_engine.render.RenderContext;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class RenderPanelController implements Initializable, PanelController {

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


    @FXML private CheckBox showWireframeCheck;
    @FXML private CheckBox useTextureCheck;
    @FXML private CheckBox useLightingCheck;

    private ToggleGroup renderGroup;

    // Пользовательские компоненты
    private ColorPalette colorPalette;
    private HueSlider hueSlider;

    @Autowired
    private RenderContext renderContext;

    private boolean updatingColor = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Настраиваем чекбоксы
        initCheckboxes();

        // 2. Настраиваем радио-кнопки
        initRadioButtons();

        // 3. Настраиваем цвет материала
        initMaterialColorControls();


        // 4. Настраиваем освещение
        initLightingControls();

        // 5. Настраиваем пользовательскую палитру
        initCustomPalette();
    }

    private void initCheckboxes() {
        if (renderContext != null) {
            showWireframeCheck.setSelected(renderContext.shouldDrawWireframe());
            useTextureCheck.setSelected(renderContext.shouldUseTexture());
            useLightingCheck.setSelected(renderContext.shouldUseLighting());
        }

        showWireframeCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (renderContext != null) {
                renderContext.setDrawWireframe(newVal);
                updateRadioButtonsFromFlags();
            }
        });

        useTextureCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (renderContext != null) {
                renderContext.setUseTexture(newVal);
                updateRadioButtonsFromFlags();
            }
        });

        useLightingCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (renderContext != null) {
                renderContext.setUseLighting(newVal);
                updateRadioButtonsFromFlags();
            }
        });
    }

    private void initRadioButtons() {
        renderGroup = new ToggleGroup();
        solidModeRadio.setToggleGroup(renderGroup);
        textureModeRadio.setToggleGroup(renderGroup);
        lightingModeRadio.setToggleGroup(renderGroup);


        updateRadioButtonsFromFlags();

        solidModeRadio.setOnAction(e -> {
            if (renderContext != null) {
                renderContext.setDrawWireframe(false);
                renderContext.setUseTexture(false);
                renderContext.setUseLighting(false);
                updateCheckboxesFromContext();
            }
        });

        textureModeRadio.setOnAction(e -> {
            if (renderContext != null) {
                renderContext.setDrawWireframe(false);
                renderContext.setUseTexture(true);
                renderContext.setUseLighting(false);
                updateCheckboxesFromContext();
            }
        });

        lightingModeRadio.setOnAction(e -> {
            if (renderContext != null) {
                renderContext.setDrawWireframe(false);
                renderContext.setUseTexture(false);
                renderContext.setUseLighting(true);
                updateCheckboxesFromContext();
            }
        });
    }


    private void updateRadioButtonsFromFlags() {
        if (renderContext == null) return;

        boolean wireframe = renderContext.shouldDrawWireframe();
        boolean texture = renderContext.shouldUseTexture();
        boolean lighting = renderContext.shouldUseLighting();

        // Логика выбора радио-кнопки
        if (wireframe && !texture && !lighting) {
            solidModeRadio.setSelected(true); // Фактически wireframe режим
        } else if (!wireframe && texture && !lighting) {
            textureModeRadio.setSelected(true);
        } else if (!wireframe && !texture && lighting) {
            lightingModeRadio.setSelected(true);
        } else {
            // Для комбинированных режимов выбираем solid как default
            solidModeRadio.setSelected(true);
        }
    }

    private void updateCheckboxesFromContext() {
        if (renderContext == null) return;

        showWireframeCheck.setSelected(renderContext.shouldDrawWireframe());
        useTextureCheck.setSelected(renderContext.shouldUseTexture());
        useLightingCheck.setSelected(renderContext.shouldUseLighting());
    }



    private void initMaterialColorControls() {
        // Начальный цвет из RenderContext
        if (renderContext != null && renderContext.getTexture() != null) {
            Color materialColor = renderContext.getMaterialColor();
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
                    if (renderContext != null && renderContext.getTexture() != null) {
                        renderContext.getTexture().setMaterialColor(color);
                    }
                } finally {
                    updatingColor = false;
                }
            }
        });
    }

    private void initLightingControls() {
        // Настройка слайдера интенсивности освещения
        lightIntensitySlider.setMin(0.0);
        lightIntensitySlider.setMax(1.0);
        lightIntensitySlider.setBlockIncrement(0.1);

        if (renderContext != null) {
            lightIntensitySlider.setValue(renderContext.getAmbientLight());
        }

        // Обновляем лейбл
        updateLightIntensityLabel(lightIntensitySlider.getValue());

        // Обработчик слайдера интенсивности
        lightIntensitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double intensity = Math.round(newVal.doubleValue() * 10.0) / 10.0;
            updateLightIntensityLabel(intensity);

            if (renderContext != null) {
                renderContext.setAmbientLight((float) intensity);
            }
        });

        // Настройка ColorPicker для цвета света
        if (renderContext != null && renderContext.getLightColor() != null) {
            lightColorPicker.setValue(renderContext.getLightColor());
        }

        // Обработчик цвета света
        lightColorPicker.setOnAction(e -> {
            Color color = lightColorPicker.getValue();
            if (renderContext != null) {
                renderContext.setLightColor(color);
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
        Color initialColor;
        if (renderContext != null && renderContext.getTexture() != null) {
            initialColor = renderContext.getMaterialColor();
        } else {
            initialColor = Color.web("#4a90e2");
        }

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
                    if (renderContext != null && renderContext.getTexture() != null) {
                        renderContext.getTexture().setMaterialColor(newVal);
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
            if (renderContext != null && renderContext.getTexture() != null) {
                renderContext.getTexture().setMaterialColor(color);
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
        lightIntensityValue.setText(String.format("%.1f", intensity));
    }

    public Parent getRenderPanel() {
        return scrollPane;
    }

    @Override
    public void onPanelShow() {
        refresh();
    }

    private void refresh() {
        if (renderContext == null) return;
        updateCheckboxesFromContext();
        updateRadioButtonsFromFlags();

        if (renderContext.getTexture() != null) {
            Color materialColor = renderContext.getMaterialColor();
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
        }

        lightIntensitySlider.setValue(renderContext.getAmbientLight());
        updateLightIntensityLabel(renderContext.getAmbientLight());

        if (renderContext.getLightColor() != null) {
            lightColorPicker.setValue(renderContext.getLightColor());
        }
    }
}