package vsu.org.ran.kgandg4.gui.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import math.vector.Vector3f;

import vsu.org.ran.kgandg4.IO.ObjWriter;
import vsu.org.ran.kgandg4.model.ModelManager;
import vsu.org.ran.kgandg4.model.models.Model;
import vsu.org.ran.kgandg4.gui.PanelController;
import vsu.org.ran.kgandg4.IO.FileDialogService;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Autowired;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;

@Component
public class TransformPanelController implements Initializable, PanelController {

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox transformPanel;

    // Перемещение
    @FXML
    private Slider translateXSlider;
    @FXML
    private Slider translateYSlider;
    @FXML
    private Slider translateZSlider;
    @FXML
    private TextField translateXField;
    @FXML
    private TextField translateYField;
    @FXML
    private TextField translateZField;
    @FXML
    private Button resetTranslateButton;

    // Поворот
    @FXML
    private Slider rotateXSlider;
    @FXML
    private Slider rotateYSlider;
    @FXML
    private Slider rotateZSlider;
    @FXML
    private TextField rotateXField;
    @FXML
    private TextField rotateYField;
    @FXML
    private TextField rotateZField;
    @FXML
    private Button resetRotateButton;

    // Масштабирование
    @FXML
    private Slider scaleXSlider;
    @FXML
    private Slider scaleYSlider;
    @FXML
    private Slider scaleZSlider;
    @FXML
    private TextField scaleXField;
    @FXML
    private TextField scaleYField;
    @FXML
    private TextField scaleZField;
    @FXML
    private Button resetScaleButton;

    // Управление
    @FXML
    private Button applyTransformButton;
    @FXML
    private Button resetAllButton;
    @FXML
    private CheckBox applyImmediatelyCheck;

    // Сохранение (теперь кнопки)
    @FXML
    private Button saveOriginalButton;
    @FXML
    private Button saveTransformedButton;

    @FXML
    public void saveOriginal() {
        saveModel(false);
        saveAsOriginal = true;
        updateSaveButtonsStyle();
    }

    @FXML
    public void saveTransformed() {
        saveModel(true);
        saveAsOriginal = false;
        updateSaveButtonsStyle();
    }

    private void updateSaveButtonsStyle() {
        if (saveAsOriginal) {
            saveOriginalButton.setStyle(
                    "-fx-font-size: 13px; " +
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
        } else {
            saveOriginalButton.setStyle(
                    "-fx-font-size: 13px; " +
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
    }

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

    private Runnable onTransformChanged; // Callback для обновления рендеринга


    private boolean isResetting = false;
    private boolean updatingFromModel = false;

    @Autowired
    private ModelManager modelManager;

    @Autowired
    private FileDialogService fileDialogService;

    @Autowired
    private AlertService alertService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Настраиваем связь слайдеров и полей ввода
        setupSliderFieldBindings();
        updateSaveButtonsStyle(); // Инициализируем стили кнопок сохранения
    }

    private void saveModel(boolean transformed) {
        if (modelManager.getCurrentModel() == null) {
            System.out.println("Нет модели для сохранения");
            if (alertService != null) {
                System.out.println("Сохранение модели: Нет активной модели для сохранения");
            }
            return;
        }

        try {
            // Показать диалог сохранения файла
            Window windowOfSave = scrollPane.getScene().getWindow();
            Optional<File> fileOpt = fileDialogService.showSaveModelDialog(windowOfSave);

            if (fileOpt.isPresent()) {
                File file = fileOpt.get();
                Path path = file.toPath();

                if (transformed) {
                    // Сохранить преобразованную модель
                    Model transformedCopy = Model.transformedModel(modelManager.getCurrentModel());
                    ObjWriter.write(transformedCopy, path);
                    System.out.println("Сохранена преобразованная модель: " + path);
                    if (alertService != null) {
                        alertService.showInfo("Сохранение",
                                "Преобразованная модель сохранена: " + file.getName());
                    }
                } else {
                    // Сохранить исходную модель
                    ObjWriter.write(modelManager.getCurrentModel(), path);
                    System.out.println("Сохранена исходная модель: " + path);
                    if (alertService != null) {
                        alertService.showInfo("Сохранение",
                                "Исходная модель сохранена: " + file.getName());
                    }
                }
            } else {
                System.out.println("Сохранение отменено пользователем");
            }

        } catch (IOException e) {
            System.err.println("Ошибка при сохранении: " + e.getMessage());
            e.printStackTrace();
            if (alertService != null) {
                alertService.showError("Ошибка сохранения",
                        "Не удалось сохранить файл: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Неожиданная ошибка: " + e.getMessage());
            e.printStackTrace();
            if (alertService != null) {
                alertService.showError("Ошибка", "Произошла непредвиденная ошибка");
            }
        }
    }
    

    private void setupSliderFieldBindings() {
        // Перемещение X
        translateXSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (updatingFromModel || isResetting) return;
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
            if (updatingFromModel || isResetting) return;
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
            if (updatingFromModel || isResetting) return;
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
            if (updatingFromModel || isResetting) return;
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
            if (updatingFromModel || isResetting) return;
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
            if (updatingFromModel || isResetting) return;
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
            if (updatingFromModel || isResetting) return;
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
            if (updatingFromModel || isResetting) return;
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
            if (updatingFromModel || isResetting) return;
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

    @FXML
    public void applyTransformations() {
        if (modelManager.getCurrentModel() == null) {
            System.out.println("Нет модели для применения трансформаций");
            if (alertService != null) {
                System.out.println("Трансформация модели: Нет активной модели");
            }
            return;
        }
        Model currentModel = modelManager.getCurrentModel();

        // Берем актуальные значения из слайдеров (на случай ручного ввода)
        translateX = (float) translateXSlider.getValue();
        translateY = (float) translateYSlider.getValue();
        translateZ = (float) translateZSlider.getValue();

        rotateX = (float) rotateXSlider.getValue();
        rotateY = (float) rotateYSlider.getValue();
        rotateZ = (float) rotateZSlider.getValue();

        scaleX = (float) scaleXSlider.getValue();
        scaleY = (float) scaleYSlider.getValue();
        scaleZ = (float) scaleZSlider.getValue();

        System.out.printf("Применение трансформаций к модели '%s': T(%.2f, %.2f, %.2f) R(%.1f, %.1f, %.1f) S(%.2f, %.2f, %.2f)%n",
                currentModel.getName(),
                translateX, translateY, translateZ,
                rotateX, rotateY, rotateZ,
                scaleX, scaleY, scaleZ);

        currentModel.setPosition(new Vector3f(translateX, translateY, translateZ));
        currentModel.setRotation(new Vector3f(rotateX, rotateY, rotateZ));
        currentModel.setScale(new Vector3f(scaleX, scaleY, scaleZ));

        notifyTransformationChanged();
    }

    @FXML
    public void resetTranslation() {
        isResetting = true;
        translateXSlider.setValue(0);
        translateYSlider.setValue(0);
        translateZSlider.setValue(0);
        translateX = 0;
        translateY = 0;
        translateZ = 0;
        translateXField.setText("0.0");
        translateYField.setText("0.0");
        translateZField.setText("0.0");
        isResetting = false;

        if (applyImmediatelyCheck.isSelected() && modelManager.getCurrentModel() != null) {
            applyTransformations();
        }
    }

    @FXML
    public void resetRotation() {
        isResetting = true;
        rotateXSlider.setValue(0);
        rotateYSlider.setValue(0);
        rotateZSlider.setValue(0);
        rotateX = 0;
        rotateY = 0;
        rotateZ = 0;
        rotateXField.setText("0.0");
        rotateYField.setText("0.0");
        rotateZField.setText("0.0");
        isResetting = false;

        if (applyImmediatelyCheck.isSelected() &&  modelManager.getCurrentModel() != null) {
            applyTransformations();
        }
    }

    @FXML
    public void resetScale() {
        isResetting = true;
        scaleXSlider.setValue(1.0);
        scaleYSlider.setValue(1.0);
        scaleZSlider.setValue(1.0);
        scaleX = 1.0f;
        scaleY = 1.0f;
        scaleZ = 1.0f;
        scaleXField.setText("1.0");
        scaleYField.setText("1.0");
        scaleZField.setText("1.0");
        isResetting = false;

        if (applyImmediatelyCheck.isSelected() &&  modelManager.getCurrentModel() != null) {
            applyTransformations();
        }
    }

    @FXML
    public void resetAllTransformations() {
        isResetting = true;
        resetTranslation();
        resetRotation();
        resetScale();
        isResetting = false;

        if (applyImmediatelyCheck.isSelected() &&  modelManager.getCurrentModel() != null) {
            applyTransformations();
        }
    }

    private void notifyTransformationChanged() {
        if (onTransformChanged != null) {
            onTransformChanged.run();
        }
    }

    @Override
    public void onPanelShow() {
        System.out.println("TransformPanelController: панель показана");
        // Сбрасываем UI только, но не применяем трансформации
        // (модель может быть null, поэтому не вызываем resetAllTransformations)
        updateUIFromCurrentModel();
    }

    private void updateUIFromCurrentModel() {
        Model currentModel = modelManager.getCurrentModel();
        if (currentModel == null) {
            resetAllUI();  // если нет модели - сбрасываем
            return;
        }

        updatingFromModel = true;
        try {
            // Получаем текущие значения из модели
            Vector3f position = currentModel.getPosition();
            Vector3f rotation = currentModel.getRotation();
            Vector3f scale = currentModel.getScale();

            // Устанавливаем значения в UI
            translateXSlider.setValue(position.getX());
            translateYSlider.setValue(position.getY());
            translateZSlider.setValue(position.getZ());
            translateXField.setText(String.format("%.2f", position.getX()));
            translateYField.setText(String.format("%.2f", position.getY()));
            translateZField.setText(String.format("%.2f", position.getZ()));

            rotateXSlider.setValue(rotation.getX());
            rotateYSlider.setValue(rotation.getY());
            rotateZSlider.setValue(rotation.getZ());
            rotateXField.setText(String.format("%.1f", rotation.getX()));
            rotateYField.setText(String.format("%.1f", rotation.getY()));
            rotateZField.setText(String.format("%.1f", rotation.getZ()));

            scaleXSlider.setValue(scale.getX());
            scaleYSlider.setValue(scale.getY());
            scaleZSlider.setValue(scale.getZ());
            scaleXField.setText(String.format("%.2f", scale.getX()));
            scaleYField.setText(String.format("%.2f", scale.getY()));
            scaleZField.setText(String.format("%.2f", scale.getZ()));

            // Обновляем локальные переменные
            translateX = position.getX();
            translateY = position.getY();
            translateZ = position.getZ();
            rotateX = rotation.getX();
            rotateY = rotation.getY();
            rotateZ = rotation.getZ();
            scaleX = scale.getX();
            scaleY = scale.getY();
            scaleZ = scale.getZ();

        } finally {
            updatingFromModel = false;
        }
    }

    private void resetAllUI() {
        // Сбрасываем только UI, без применения трансформаций
        isResetting = true;

        translateXSlider.setValue(0);
        translateYSlider.setValue(0);
        translateZSlider.setValue(0);
        translateX = 0;
        translateY = 0;
        translateZ = 0;
        translateXField.setText("0.0");
        translateYField.setText("0.0");
        translateZField.setText("0.0");

        rotateXSlider.setValue(0);
        rotateYSlider.setValue(0);
        rotateZSlider.setValue(0);
        rotateX = 0;
        rotateY = 0;
        rotateZ = 0;
        rotateXField.setText("0.0");
        rotateYField.setText("0.0");
        rotateZField.setText("0.0");

        scaleXSlider.setValue(1.0);
        scaleYSlider.setValue(1.0);
        scaleZSlider.setValue(1.0);
        scaleX = 1.0f;
        scaleY = 1.0f;
        scaleZ = 1.0f;
        scaleXField.setText("1.0");
        scaleYField.setText("1.0");
        scaleZField.setText("1.0");

        isResetting = false;
    }
}