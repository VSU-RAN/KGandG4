package vsu.org.ran.kgandg4.gui.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.Initializable;

import vsu.org.ran.kgandg4.model.ModelManager;
import math.vector.Vector3f;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Autowired;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;
import vsu.org.ran.kgandg4.gui.PanelController;
import vsu.org.ran.kgandg4.gui.ConstantsAndStyles;
import vsu.org.ran.kgandg4.model.models.Model;

@Component
public class EditPanelController implements Initializable, PanelController {

    @FXML private Label modelStatsLabel;
    @FXML private Label selectionInfoLabel;
    @FXML private TextField selectedIndexField;
    @FXML private ToggleGroup deleteTypeToggleGroup;
    @FXML private RadioButton deleteVertexRadio;
    @FXML private RadioButton deletePolygonRadio;

    @Autowired
    private ModelManager modelManager;

    @Autowired
    private AlertService alertService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateModelStats();

        selectionInfoLabel.textProperty().bind(modelManager.selectionInfoProperty());

        modelManager.selectedVertexIndexProperty().addListener((obs, oldVal, newVal) -> {
            updateSelectionStyle();
        });
        modelManager.selectedPolygonIndexProperty().addListener((obs, oldVal, newVal) -> {
            updateSelectionStyle();
        });

        modelManager.activeModelProperty().addListener((obs, oldModel, newModel) -> {
            updateModelStats();
            updateSelectionStyle();
            updateElementCounts();
        });

        // Устанавливаем вершину по умолчанию как выбранную
        deleteVertexRadio.setSelected(true);

        // Обновляем информацию о количестве элементов при изменении модели
        updateElementCounts();
    }

    private void updateSelectionStyle() {
        if (modelManager.isVertexSelected()) {
            selectionInfoLabel.setStyle("-fx-text-fill: #007bff; -fx-font-weight: bold;");
        } else if (modelManager.isPolygonSelected()) {
            selectionInfoLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
        } else {
            selectionInfoLabel.setStyle("-fx-text-fill: #495057;");
        }
    }

    private void updateElementCounts() {
        Model currentModel = modelManager.getCurrentModel();
        if (currentModel != null) {
            deleteVertexRadio.setText("Вершина (всего: " + currentModel.getVertices().size() + ")");
            deletePolygonRadio.setText("Полигон (всего: " + currentModel.getPolygons().size() + ")");
        } else {
            deleteVertexRadio.setText("Вершина");
            deletePolygonRadio.setText("Полигон");
        }
    }

    /**
     * Обработка клика по модели
     */
    public void handleModelClick(Vector3f clickPoint) {
        modelManager.handleClickInModel(clickPoint);
        updateModelStats();
        updateElementCounts();
    }

    /**
     * Удаляет выбранный элемент
     */
    @FXML
    public void deleteSelected() {
        try {
            boolean deleted = modelManager.deleteSelected();

            if (deleted) {
                alertService.showInfo("Успех", "Элемент удален");
            } else {
                alertService.showInfo("Предупреждение", "Не выбран элемент для удаления");
            }

            updateModelStats();
            updateElementCounts();
        } catch (Exception e) {
            showError("Ошибка при удалении выбранного элемента", e);
        }
    }

    /**
     * Удаляет всю модель
     */
    @FXML
    public void deleteAll() {
        if (modelManager.getCurrentModel() == null) {
            alertService.showInfo("Предупреждение", "Нет активной модели");
            return;
        }

        Model currentModel = modelManager.getCurrentModel();

        // Временный вариант - без подтверждения
        boolean confirmed = true; // Всегда подтверждаем для теста

        // Если нужны подтверждения позже:
        // boolean confirmed = alertService.showConfirmation("Подтверждение", "...");

        if (confirmed) {
            boolean deleted = modelManager.deleteCurrentModel();

            if (deleted) {
                alertService.showInfo("Успех", "Модель '" + currentModel.getName() + "' успешно удалена");
            } else {
                alertService.showError("Ошибка", "Не удалось удалить модель");
            }
        }

        updateModelStats();
        updateElementCounts();
    }

    /**
     * Удаляет элемент по индексу
     */
    @FXML
    public void deleteByIndex() {
        try {
            String indexText = selectedIndexField.getText().trim();
            if (indexText.isEmpty()) {
                alertService.showError("Ошибка", "Введите индекс");
                return;
            }

            int index = Integer.parseInt(indexText);

            // Проверяем, есть ли модель
            if (modelManager.getCurrentModel() == null) {
                alertService.showError("Ошибка", "Нет активной модели");
                return;
            }

            Model currentModel = modelManager.getCurrentModel();

            // Определяем тип элемента из RadioButton
            boolean isVertex = deleteVertexRadio.isSelected();

            boolean deleted = false;
            String successMessage = "";

            if (isVertex) {
                // Проверяем, существует ли такая вершина
                if (index < 0 || index >= currentModel.getVertices().size()) {
                    alertService.showError("Ошибка",
                            String.format("Вершина #%d не существует. Всего вершин: %d",
                                    index, currentModel.getVertices().size()));
                    return;
                }

                // Без подтверждения для теста
                deleted = modelManager.deleteVertexByIndex(index);
                successMessage = "Вершина #" + index + " удалена";

            } else {
                // Удаление полигона
                if (index < 0 || index >= currentModel.getPolygons().size()) {
                    alertService.showError("Ошибка",
                            String.format("Полигон #%d не существует. Всего полигонов: %d",
                                    index, currentModel.getPolygons().size()));
                    return;
                }

                // Без подтверждения для теста
                deleted = modelManager.deletePolygonByIndex(index);
                successMessage = "Полигон #" + index + " удален";
            }

            if (deleted) {
                alertService.showInfo("Успех", successMessage);
                updateElementCounts(); // Обновляем счетчики
            } else {
                alertService.showError("Ошибка", "Не удалось удалить элемент");
            }

            selectedIndexField.clear();
            updateModelStats();

        } catch (NumberFormatException e) {
            alertService.showError("Ошибка", "Введите корректный числовой индекс");
        } catch (Exception e) {
            showError("Ошибка при удалении по индексу", e);
        }
    }

    /**
     * Очищает текущий выбор
     */
    @FXML
    public void clearSelection() {
        try {
            modelManager.clearSelection();
        } catch (Exception e) {
            showError("Ошибка при очистке выбора", e);
        }
    }

    /**
     * Обновляет статистику модели
     */
    public void updateModelStats() {
        try {
            if (modelManager.getCurrentModel() != null) {
                Model currentModel = modelManager.getCurrentModel();
                modelStatsLabel.setText(currentModel.toString());
            } else {
                modelStatsLabel.setText(ConstantsAndStyles.DEFAULT_MODEL_TEXT);
            }
        } catch (Exception e) {
            modelStatsLabel.setText("Ошибка загрузки статистики");
            e.printStackTrace();
        }
    }

    @Override
    public void onPanelShow() {
        updateModelStats();
        updateElementCounts();
    }

    /**
     * Показывает ошибку в консоли
     */
    private void showError(String message, Exception e) {
        System.err.println(message + ": " + e.getMessage());
        e.printStackTrace();
        alertService.showError("Ошибка", message + ": " + e.getMessage());
    }
}