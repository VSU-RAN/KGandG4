package vsu.org.ran.kgandg4.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import vsu.org.ran.kgandg4.controllers.GuiController;

import java.net.URL;
import java.util.ResourceBundle;

public class TransformPanelController implements Initializable {

    @FXML private ScrollPane scrollPane;
    @FXML private VBox transformPanel;

    // Перемещение
    @FXML private Slider translateXSlider;
    @FXML private Slider translateYSlider;
    @FXML private Slider translateZSlider;
    @FXML private TextField translateXField;
    @FXML private TextField translateYField;
    @FXML private TextField translateZField;
    @FXML private Button resetTranslateButton;

    // Поворот
    @FXML private Slider rotateXSlider;
    @FXML private Slider rotateYSlider;
    @FXML private Slider rotateZSlider;
    @FXML private TextField rotateXField;
    @FXML private TextField rotateYField;
    @FXML private TextField rotateZField;
    @FXML private Button resetRotateButton;

    // Масштабирование
    @FXML private Slider scaleXSlider;
    @FXML private Slider scaleYSlider;
    @FXML private Slider scaleZSlider;
    @FXML private TextField scaleXField;
    @FXML private TextField scaleYField;
    @FXML private TextField scaleZField;
    @FXML private Button resetScaleButton;

    // Управление
    @FXML private Button applyTransformButton;
    @FXML private Button resetAllButton;
    @FXML private CheckBox applyImmediatelyCheck;

    // Сохранение (теперь кнопки)
    @FXML private Button saveOriginalButton;
    @FXML private Button saveTransformedButton;

    private GuiController guiController;

    // Текущие значения трансформаций
    private float translateX = 0.0f;
    private float translateY = 0.0f;
    private float translateZ = 0.0f;
    private float rotateX = 0.0f;
    private float rotateY = 0.0f;
    private float rotateZ = 0.0f;
    private float scaleX = 1.0f;
    private float scaleY = 1.0f;
    private float scaleZ = 1.0f;

    private boolean saveAsOriginal = true;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Инициализация TransformPanelController...");

        // Настраиваем связь слайдеров и полей ввода
        setupSliderFieldBindings();

        // Назначаем обработчик для чекбокса
        applyImmediatelyCheck.setOnAction(e -> onApplyImmediatelyChanged());

        System.out.println("✓ TransformPanelController инициализирован");
    }

    private void setupSliderFieldBindings() {
        // Перемещение X
        translateXSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            translateX = newVal.floatValue();
            translateXField.setText(String.format("%.2f", translateX));
            if (applyImmediatelyCheck.isSelected()) {
                applyTransformations();
            }
        });
        translateXField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                translateX = Float.parseFloat(newVal);
                translateXSlider.setValue(translateX);
            } catch (NumberFormatException e) {
                // Игнорируем некорректный ввод
            }
        });

        // Перемещение Y
        translateYSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            translateY = newVal.floatValue();
            translateYField.setText(String.format("%.2f", translateY));
            if (applyImmediatelyCheck.isSelected()) {
                applyTransformations();
            }
        });
        translateYField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                translateY = Float.parseFloat(newVal);
                translateYSlider.setValue(translateY);
            } catch (NumberFormatException e) {
                // Игнорируем некорректный ввод
            }
        });

        // Перемещение Z
        translateZSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            translateZ = newVal.floatValue();
            translateZField.setText(String.format("%.2f", translateZ));
            if (applyImmediatelyCheck.isSelected()) {
                applyTransformations();
            }
        });
        translateZField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                translateZ = Float.parseFloat(newVal);
                translateZSlider.setValue(translateZ);
            } catch (NumberFormatException e) {
                // Игнорируем некорректный ввод
            }
        });

        // Поворот X
        rotateXSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            rotateX = newVal.floatValue();
            rotateXField.setText(String.format("%.1f", rotateX));
            if (applyImmediatelyCheck.isSelected()) {
                applyTransformations();
            }
        });
        rotateXField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                rotateX = Float.parseFloat(newVal);
                rotateXSlider.setValue(rotateX);
            } catch (NumberFormatException e) {
                // Игнорируем некорректный ввод
            }
        });

        // Поворот Y
        rotateYSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            rotateY = newVal.floatValue();
            rotateYField.setText(String.format("%.1f", rotateY));
            if (applyImmediatelyCheck.isSelected()) {
                applyTransformations();
            }
        });
        rotateYField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                rotateY = Float.parseFloat(newVal);
                rotateYSlider.setValue(rotateY);
            } catch (NumberFormatException e) {
                // Игнорируем некорректный ввод
            }
        });

        // Поворот Z
        rotateZSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            rotateZ = newVal.floatValue();
            rotateZField.setText(String.format("%.1f", rotateZ));
            if (applyImmediatelyCheck.isSelected()) {
                applyTransformations();
            }
        });
        rotateZField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                rotateZ = Float.parseFloat(newVal);
                rotateZSlider.setValue(rotateZ);
            } catch (NumberFormatException e) {
                // Игнорируем некорректный ввод
            }
        });

        // Масштаб X
        scaleXSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            scaleX = newVal.floatValue();
            scaleXField.setText(String.format("%.2f", scaleX));
            if (applyImmediatelyCheck.isSelected()) {
                applyTransformations();
            }
        });
        scaleXField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                scaleX = Float.parseFloat(newVal);
                scaleXSlider.setValue(scaleX);
            } catch (NumberFormatException e) {
                // Игнорируем некорректный ввод
            }
        });

        // Масштаб Y
        scaleYSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            scaleY = newVal.floatValue();
            scaleYField.setText(String.format("%.2f", scaleY));
            if (applyImmediatelyCheck.isSelected()) {
                applyTransformations();
            }
        });
        scaleYField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                scaleY = Float.parseFloat(newVal);
                scaleYSlider.setValue(scaleY);
            } catch (NumberFormatException e) {
                // Игнорируем некорректный ввод
            }
        });

        // Масштаб Z
        scaleZSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            scaleZ = newVal.floatValue();
            scaleZField.setText(String.format("%.2f", scaleZ));
            if (applyImmediatelyCheck.isSelected()) {
                applyTransformations();
            }
        });
        scaleZField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                scaleZ = Float.parseFloat(newVal);
                scaleZSlider.setValue(scaleZ);
            } catch (NumberFormatException e) {
                // Игнорируем некорректный ввод
            }
        });
    }

    private void onApplyImmediatelyChanged() {
        System.out.println("Автоприменение: " + applyImmediatelyCheck.isSelected());
    }

    @FXML
    public void applyTransformations() {
        System.out.println("Применение трансформаций:");
        System.out.println("  Перемещение: X=" + translateX + ", Y=" + translateY + ", Z=" + translateZ);
        System.out.println("  Поворот: X=" + rotateX + ", Y=" + rotateY + ", Z=" + rotateZ);
        System.out.println("  Масштаб: X=" + scaleX + ", Y=" + scaleY + ", Z=" + scaleZ);

        if (guiController != null) {
            // Здесь нужно вызвать метод GuiController для применения трансформаций
            // guiController.applyModelTransformations(translateX, translateY, translateZ,
            //                                          rotateX, rotateY, rotateZ,
            //                                          scaleX, scaleY, scaleZ);
        }
    }

    @FXML
    public void resetTranslation() {
        translateXSlider.setValue(0);
        translateYSlider.setValue(0);
        translateZSlider.setValue(0);
        translateXField.setText("0.0");
        translateYField.setText("0.0");
        translateZField.setText("0.0");
        System.out.println("Перемещение сброшено");
    }

    @FXML
    public void resetRotation() {
        rotateXSlider.setValue(0);
        rotateYSlider.setValue(0);
        rotateZSlider.setValue(0);
        rotateXField.setText("0.0");
        rotateYField.setText("0.0");
        rotateZField.setText("0.0");
        System.out.println("Поворот сброшен");
    }

    @FXML
    public void resetScale() {
        scaleXSlider.setValue(1.0);
        scaleYSlider.setValue(1.0);
        scaleZSlider.setValue(1.0);
        scaleXField.setText("1.0");
        scaleYField.setText("1.0");
        scaleZField.setText("1.0");
        System.out.println("Масштаб сброшен");
    }

    @FXML
    public void resetAllTransformations() {
        resetTranslation();
        resetRotation();
        resetScale();
        System.out.println("Все трансформации сброшены");
    }

    @FXML
    public void saveOriginal() {
        saveAsOriginal = true;
        System.out.println("Режим сохранения: Исходная модель");
        // Подсветка активной кнопки
        saveOriginalButton.setStyle("-fx-font-size: 13px; " +
                "-fx-background-color: #138496; " +
                "-fx-text-fill: white; " +
                "-fx-border-radius: 4; " +
                "-fx-background-radius: 4; " +
                "-fx-cursor: hand;");
        saveTransformedButton.setStyle("-fx-font-size: 13px; " +
                "-fx-background-color: #fd7e14; " +
                "-fx-text-fill: white; " +
                "-fx-border-radius: 4; " +
                "-fx-background-radius: 4; " +
                "-fx-cursor: hand;");
    }

    @FXML
    public void saveTransformed() {
        saveAsOriginal = false;
        System.out.println("Режим сохранения: С трансформациями");
        // Подсветка активной кнопки
        saveOriginalButton.setStyle("-fx-font-size: 13px; " +
                "-fx-background-color: #17a2b8; " +
                "-fx-text-fill: white; " +
                "-fx-border-radius: 4; " +
                "-fx-background-radius: 4; " +
                "-fx-cursor: hand;");
        saveTransformedButton.setStyle("-fx-font-size: 13px; " +
                "-fx-background-color: #e46c0a; " +
                "-fx-text-fill: white; " +
                "-fx-border-radius: 4; " +
                "-fx-background-radius: 4; " +
                "-fx-cursor: hand;");
    }

    public void setGuiController(GuiController guiController) {
        this.guiController = guiController;
        System.out.println("✓ GuiController установлен в TransformPanelController");
    }

    public Parent getTransformPanel() {
        return scrollPane;
    }

    // Геттеры для настроек сохранения
    public boolean isSaveOriginal() {
        return saveAsOriginal;
    }

    public boolean isSaveTransformed() {
        return !saveAsOriginal;
    }
}